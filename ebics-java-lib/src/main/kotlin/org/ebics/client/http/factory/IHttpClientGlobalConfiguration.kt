package org.ebics.client.http.factory

import org.ebics.client.http.client.IHttpClientConfiguration

interface IHttpClientGlobalConfiguration {
    val connectionPoolMaxTotal: Int
    val connectionPoolDefaultMaxPerRoute: Int
    val configurations: Map<String, IHttpClientConfiguration>
}