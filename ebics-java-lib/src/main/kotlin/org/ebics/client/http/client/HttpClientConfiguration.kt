package org.ebics.client.http.client

class HttpClientConfiguration(
    override val displayName: String = "default",
    override val sslTrustedStoreFile: String? = null,
    override val sslTrustedStoreFilePassword: String?  = null,
    override val httpProxyHost: String? = null,
    override val httpProxyPort: Int? = null,
    override val httpProxyUser: String? = null,
    override val httpProxyPassword: String? = null,
    override val httpContentType: String? = null,
    override val socketTimeoutMilliseconds: Int = 300 * 1000,
    override val connectionTimeoutMilliseconds: Int = 300 * 1000
) : IHttpClientConfiguration