package org.ebics.client.http.factory

import org.ebics.client.api.trace.IBankConnectionTraceSession
import org.ebics.client.http.client.HttpClient
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory

interface ITraceableHttpClientFactory<T: HttpClient> : IHttpClientFactory<T> {

    /**
     * Trace the request before its send, and then trace the response,
     * In case of any exception is the exception traced
     */
    fun sendAndTraceRequest(httpTransferSession: IHttpTransferSession, traceSession: IBankConnectionTraceSession, contentFactory: ContentFactory): ByteArrayContentFactory
}