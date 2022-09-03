/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
package org.ebics.client.keymgmt.h004

import org.ebics.client.api.EbicsSession
import org.ebics.client.api.trace.BankConnectionTraceSession
import org.ebics.client.api.trace.TraceManager
import org.ebics.client.certificate.BankCertificateManager
import org.ebics.client.certificate.BankCertificateManager.Companion.createFromCertificates
import org.ebics.client.certificate.BankCertificateManager.Companion.createFromPubKeyExponentAndModulus
import org.ebics.client.exception.EbicsException
import org.ebics.client.http.client.TraceableHttpClient
import org.ebics.client.http.factory.HttpTransferSession
import org.ebics.client.http.factory.ITraceableHttpClientFactory
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.keymgmt.KeyManagement
import org.ebics.client.model.user.EbicsUserAction
import org.ebics.client.utils.Utils
import org.ebics.client.xml.h004.*
import org.springframework.stereotype.Component
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Everything that has to do with key handling.
 * If you have a totally new account use `sendINI()` and `sendHIA()` to send you newly created keys to the bank.
 * Then wait until the bank activated your keys.
 * If you are migrating from FTAM. Just send HPB, your EBICS account should be usable without delay.
 *
 * @author Hachani
 */
@Component("H004.KeyManagement")
class KeyManagement(
    private val httpClient: ITraceableHttpClientFactory<TraceableHttpClient>,
    private val traceManager: TraceManager
) : KeyManagement {
    /**
     * Sends the user's signature key (A005) to the bank.
     * After successful operation the user is in state "initialized".
     * @param ebicsSession the ebics session
     * @throws EbicsException server generated error message
     * @throws IOException communication error
     */
    @Throws(EbicsException::class, IOException::class)
    override fun sendINI(ebicsSession: EbicsSession, traceSession: BankConnectionTraceSession) {
        val response: KeyManagementResponseElement
        ebicsSession.user.checkAction(EbicsUserAction.INI)
        val httpSession = HttpTransferSession(ebicsSession)
        val request = INIRequestElement(ebicsSession).apply { build(); validate() }
        traceManager.trace(ByteArrayContentFactory(request.signaturePubKey.toByteArray()), traceSession, false)

        val responseBody = httpClient.sendAndTraceRequest(httpSession, traceSession, ByteArrayContentFactory(request.prettyPrint()))
        
        response = KeyManagementResponseElement(responseBody)
        traceManager.callAndUpdateLastTrace(traceSession) {
            response.build()
            response.report()
        }
        ebicsSession.user.updateStatus(EbicsUserAction.INI)
    }

    /**
     * Sends the public part of the protocol keys to the bank.
     * @param orderId the order ID. Let it null to generate a random one.
     * @throws IOException communication error
     * @throws EbicsException server generated error message
     */
    @Throws(IOException::class, EbicsException::class)
    override fun sendHIA(ebicsSession: EbicsSession, traceSession: BankConnectionTraceSession) {
        val response: KeyManagementResponseElement
        ebicsSession.user.checkAction(EbicsUserAction.HIA)
        val httpSession = HttpTransferSession(ebicsSession)
        val request = HIARequestElement(ebicsSession).apply { build(); validate() }
        traceManager.trace(ByteArrayContentFactory(request.requestOrderData.toByteArray()), traceSession, false)
        val responseBody = httpClient.sendAndTraceRequest(httpSession, traceSession, ByteArrayContentFactory(request.prettyPrint()))
        
        response = KeyManagementResponseElement(responseBody)
        traceManager.callAndUpdateLastTrace(traceSession) {
            response.build()
            response.report()
        }
        ebicsSession.user.updateStatus(EbicsUserAction.HIA)
    }

    /**
     * Sends encryption and authentication keys to the bank.
     * This order is only allowed for a new user at the bank side that has been created by copying the A005 key.
     * The keys will be activated immediately after successful completion of the transfer.
     * @param ebicsSession the ebicsSession
     * @throws IOException communication error
     * @throws GeneralSecurityException data decryption error
     * @throws EbicsException server generated error message
     */
    @Throws(IOException::class, GeneralSecurityException::class, EbicsException::class)
    override fun sendHPB(ebicsSession: EbicsSession, traceSession: BankConnectionTraceSession, password: String): BankCertificateManager {
        ebicsSession.user.checkAction(EbicsUserAction.HPB)
        val httpSession = HttpTransferSession(ebicsSession)
        val request = HPBRequestElement(ebicsSession).apply { build(); validate() }

        val responseBody = httpClient.sendAndTraceRequest(httpSession, traceSession, ByteArrayContentFactory(request.prettyPrint()))
        val response = KeyManagementResponseElement(responseBody)
        traceManager.callAndUpdateLastTrace(traceSession) {
            response.build()
            response.report()
        }
        val factory: ContentFactory =
            ByteArrayContentFactory(Utils.unzip(ebicsSession.userCert.decrypt(response.orderData, response.transactionKey)))
        val orderData = HPBResponseOrderDataElement(factory)
        orderData.build()
        traceManager.trace(ByteArrayContentFactory(orderData.toByteArray()), traceSession, false)
        val manager: BankCertificateManager = if (ebicsSession.user.useCertificate) createFromCertificates(
            orderData.bankE002Certificate,
            orderData.bankX002Certificate
        ) else createFromPubKeyExponentAndModulus(
            orderData.bankE002PublicKeyExponent, orderData.bankE002PublicKeyModulus,
            orderData.bankX002PublicKeyExponent, orderData.bankX002PublicKeyModulus
        )
        ebicsSession.user.updateStatus(EbicsUserAction.HPB)
        return manager
    }

    /**
     * Sends the SPR order to the bank.
     * After that you have to start over with sending INI and HIA.
     * @param ebicsSession the ebicsSession
     * @throws IOException Communication exception
     * @throws EbicsException Error message generated by the bank.
     */
    @Throws(IOException::class, EbicsException::class)
    override fun lockAccess(ebicsSession: EbicsSession, traceSession: BankConnectionTraceSession) {
        ebicsSession.user.checkAction(EbicsUserAction.SPR)
        val httpSession = HttpTransferSession(ebicsSession)
        val request = SPRRequestElement(ebicsSession).apply { build(); validate() }

        val responseBody = httpClient.sendAndTraceRequest(httpSession, traceSession, ByteArrayContentFactory(request.prettyPrint()))
        val response = SPRResponseElement(responseBody)
        traceManager.callAndUpdateLastTrace(traceSession) {
            response.build()
            response.report()
        }
        ebicsSession.user.updateStatus(EbicsUserAction.SPR)
    }
}