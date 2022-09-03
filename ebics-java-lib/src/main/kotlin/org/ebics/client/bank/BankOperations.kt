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
package org.ebics.client.bank

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.EbicsConfiguration
import org.ebics.client.api.trace.TraceManager
import org.ebics.client.api.trace.h000.BankTraceSession
import org.ebics.client.exception.EbicsException
import org.ebics.client.http.client.TraceableHttpClient
import org.ebics.client.http.client.request.HttpClientRequest
import org.ebics.client.http.factory.ITraceableHttpClientFactory
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.model.EbicsVersion
import org.ebics.client.xml.h000.HEVRequest
import org.ebics.client.xml.h000.HEVResponse
import org.springframework.stereotype.Component
import java.io.IOException

@Component("H000.BankOperations")
class BankOperations(
    val configuration: EbicsConfiguration,
    val httpClient: ITraceableHttpClientFactory<TraceableHttpClient>,
    val traceManager: TraceManager
) {
    @Throws(EbicsException::class, IOException::class)
    fun sendHEV(bank: EbicsBank, traceSession: BankTraceSession, configurationName: String = "default"): List<EbicsVersion> {
        val request = HEVRequest(bank.hostId).apply { build(); validate() }
        val responseBody = httpClient.getHttpClient(configurationName)
            .sendAndTrace(HttpClientRequest(bank.bankURL, ByteArrayContentFactory(request.prettyPrint())), traceSession)
        val response = HEVResponse(responseBody).apply {
            traceManager.callAndUpdateLastTrace(traceSession) {
                build()
                validate()
                report()
            }
        }
        return response.getSupportedVersions()
    }
}