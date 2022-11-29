package org.ebics.client.http.client.request

import org.ebics.client.interfaces.ContentFactory
import java.net.URL

data class HttpClientRequest  (
    override val requestURL: URL,
    override val content: ContentFactory,
) : IHttpClientRequest