package org.ebics.client.http.factory

import org.ebics.client.http.client.HttpClientConfiguration

interface HttpClientGlobalConfiguration {
    val connectionPoolMaxTotal: Int
    val connectionPoolDefaultMaxPerRoute: Int
    val configurations: Map<String, HttpClientConfiguration>
}