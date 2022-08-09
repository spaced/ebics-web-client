package org.ebics.client.http.client.request

import org.ebics.client.interfaces.ContentFactory
import java.net.URL

interface IHttpClientRequest {
    val requestURL: URL
    val content: ContentFactory
}