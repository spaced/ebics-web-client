package org.ebics.client.http.client

import org.apache.http.impl.client.CloseableHttpClient
import org.ebics.client.api.trace.IBaseTraceSession
import org.ebics.client.api.trace.TraceManager
import org.ebics.client.exception.EbicsException
import org.ebics.client.http.client.request.HttpClientRequest
import org.ebics.client.io.ByteArrayContentFactory

class TraceableHttpClient(
    httpClient: CloseableHttpClient,
    configuration: HttpClientRequestConfiguration,
    configurationName: String = "default",
    val traceManager: TraceManager,
) : SimpleHttpClient(httpClient, configuration, configurationName), ITraceableHttpClient {

    override fun sendAndTrace(request: HttpClientRequest, traceSession: IBaseTraceSession): ByteArrayContentFactory {
        try {
            traceManager.trace(request.content, traceSession, request = true)
            val response = send(request)
            traceManager.trace(response, traceSession, request = false)
            return response
        } catch (e: EbicsException) {
            traceManager.traceException(e, traceSession)
            throw e
        }
    }
}