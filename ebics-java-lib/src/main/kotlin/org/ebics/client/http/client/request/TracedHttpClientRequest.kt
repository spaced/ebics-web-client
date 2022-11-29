package org.ebics.client.http.client.request

import org.ebics.client.api.trace.IBankConnectionTraceSession
import org.ebics.client.interfaces.ContentFactory
import java.net.URL

data class TracedHttpClientRequest(
    override val requestURL: URL,
    override val content: ContentFactory,
    val traceSession: IBankConnectionTraceSession
) : IHttpClientRequest