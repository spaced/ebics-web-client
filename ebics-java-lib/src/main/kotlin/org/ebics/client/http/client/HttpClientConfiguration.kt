package org.ebics.client.http.client

class HttpClientConfiguration(
    override var displayName: String = "default",
    override var sslTrustedStoreFile: String? = null,
    override var sslTrustedStoreFilePassword: String?  = null,
    override var httpProxyHost: String? = null,
    override var httpProxyPort: Int? = null,
    override var httpProxyUser: String? = null,
    override var httpProxyPassword: String? = null,
    override var httpContentType: String? = null,
    override var socketTimeoutMilliseconds: Int = 300 * 1000,
    override var connectionTimeoutMilliseconds: Int = 300 * 1000
) : IHttpClientConfiguration