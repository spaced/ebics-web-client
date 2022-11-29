package org.ebics.client.http.client

import org.ebics.client.api.trace.IBaseTraceSession
import org.ebics.client.http.client.request.HttpClientRequest
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory
import java.net.URL

interface ITraceableHttpClient : HttpClient {
    fun sendAndTrace(request: HttpClientRequest, traceSession: IBaseTraceSession): ByteArrayContentFactory
    fun sendAndTrace(
        requestURL: URL,
        content: ContentFactory,
        traceSession: IBaseTraceSession
    ) = sendAndTrace(HttpClientRequest(requestURL, content), traceSession)
}