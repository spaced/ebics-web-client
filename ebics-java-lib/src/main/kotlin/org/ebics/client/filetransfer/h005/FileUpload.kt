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
package org.ebics.client.filetransfer.h005

import org.ebics.client.api.EbicsSession
import org.ebics.client.api.TransferState
import org.ebics.client.api.trace.IBankConnectionTraceSession
import org.ebics.client.api.trace.TraceManager
import org.ebics.client.exception.EbicsException
import org.ebics.client.http.client.TraceableHttpClient
import org.ebics.client.http.factory.HttpTransferSession
import org.ebics.client.http.factory.ITraceableHttpClientFactory
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.h005.*
import org.ebics.client.utils.toHexString
import org.ebics.client.xml.h005.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException

/**
 * Handling of file transfers.
 * Files can be transferred to and fetched from the bank.
 * Every transfer may be performed in a recoverable way.
 * For convenience and performance reasons there are also
 * methods that do the whole transfer in one method call.
 * To use the recoverable transfer mode, you may set a working
 * directory for temporarily created files.
 *
 *
 *  EBICS specification 2.4.2 - 6.2 Encryption at application level
 *
 *
 * In the event of an upload transaction, a random symmetrical key is generated in the
 * customer system that is used exclusively within the framework of this transaction both for
 * encryption of the ES’s and for encryption of the order data. This key is encrypted
 * asymmetrically with the financial institution’s public encryption key and is transmitted by the
 * customer system to the bank system during the initialization phase of the transaction.
 *
 *
 * Analogously, in the case of a download transaction a random symmetrical key is generated
 * in the bank system that is used for encryption of the order data that is to be downloaded and
 * for encryption of the bank-technical signature that has been provided by the financial
 * institution. This key is asymmetrically encrypted and is transmitted by the bank system to the
 * customer system during the initialization phase of the transaction. The asymmetrical
 * encryption takes place with the technical subscriber’s public encryption key if the
 * transaction’s EBICS messages are sent by a technical subscriber. Otherwise the
 * asymmetrical encryption takes place with the public encryption key of the non-technical
 * subscriber, i.e. the submitter of the order.
 *
 * @author Hachani
 */
@Component("H005.FileUpload")
class FileUpload(
    private val httpClient: ITraceableHttpClientFactory<TraceableHttpClient>,
    private val traceManager: TraceManager
) {
    /**
     * Initiates a file transfer to the bank.
     * @param content The bytes you want to send.
     * @param ebicsUploadOrder As which order details
     * @throws IOException
     * @throws EbicsException
     */
    @Throws(IOException::class, EbicsException::class)
    fun sendFile(
        session: EbicsSession,
        traceSession: IBankConnectionTraceSession,
        content: ByteArray,
        ebicsUploadOrder: EbicsUploadOrder
    ): EbicsUploadOrderResponse {
        logger.info("Start uploading file via EBICS sessionId=${session.sessionId}, userId=${session.user.userId}, partnerId=${session.user.partner.partnerId}, bankURL=${session.user.partner.bank.bankURL}, order=$ebicsUploadOrder, file length=${content.size}")
        val orderType = ebicsUploadOrder.adminOrderType
        val httpSession = HttpTransferSession(session)
        val initializer = UploadInitializationRequestElement(
            session,
            ebicsUploadOrder,
            content
        ).apply { build(); validate() }

        traceManager.trace(ByteArrayContentFactory(initializer.userSignature.toByteArray()), traceSession)

        val responseBody = httpClient.sendAndTraceRequest(
            httpSession,
            traceSession,
            ByteArrayContentFactory(initializer.prettyPrint())
        )

        val response = InitializationResponseElement(
            responseBody,
            orderType
        )
        traceManager.callAndUpdateLastTrace(traceSession) {
            response.build()
        }

        val state = TransferState(initializer.segmentNumber, response.transactionId)
        while (state.hasNext()) {
            val segmentNumber = state.next()
            sendFileSegment(
                session,
                initializer.getContent(segmentNumber), segmentNumber, state.isLastSegment,
                state.transactionId, orderType, traceSession, httpSession
            )
        }
        logger.info("Finished uploading file via EBICS sessionId=${session.sessionId}, userId=${session.user.userId}, partnerId=${session.user.partner.partnerId}, bankURL=${session.user.partner.bank.bankURL}, order=$ebicsUploadOrder, file length=${content.size}, orderNumber=${response.orderNumber}, transactionId=${response.transactionId.toHexString()}")
        return EbicsUploadOrderResponse(response.orderNumber, response.transactionId.toHexString())
    }

    /**
     * Sends a segment to the ebics bank server.
     * @param contentFactory the content factory that contain the segment data.
     * @param segmentNumber the segment number
     * @param lastSegment is it the last segment?
     * @param transactionId the transaction Id
     * @param orderType the order type
     * @throws IOException
     * @throws EbicsException
     */
    @Throws(IOException::class, EbicsException::class)
    private fun sendFileSegment(
        session: EbicsSession,
        contentFactory: ContentFactory,
        segmentNumber: Int,
        lastSegment: Boolean,
        transactionId: ByteArray,
        orderType: EbicsAdminOrderType,
        traceSession: IBankConnectionTraceSession,
        httpSession: HttpTransferSession
    ) {
        val segmentStr = if (lastSegment) "last segment ($segmentNumber)" else "segment ($segmentNumber)"
        logger.info(
            "Uploading $segmentStr of file via EBICS sessionId=${session.sessionId}, userId=${session.user.userId}, partnerId=${session.user.partner.partnerId}, bankURL=${session.user.partner.bank.bankURL}, segmentLength=${contentFactory.content.available()} Bytes"
        )
        val uploader = UploadTransferRequestElement(
            session,
            orderType,
            segmentNumber,
            lastSegment,
            transactionId,
            contentFactory
        ).apply { build(); validate() }

        val responseBody =
            httpClient.sendAndTraceRequest(httpSession, traceSession, ByteArrayContentFactory(uploader.prettyPrint()))

        val response = TransferResponseElement(responseBody)
        traceManager.callAndUpdateLastTrace(traceSession) {
            response.build()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FileUpload::class.java)
    }
}