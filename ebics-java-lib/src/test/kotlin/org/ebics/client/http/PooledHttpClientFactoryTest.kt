package org.ebics.client.http

import org.ebics.client.http.client.HttpClientConfiguration
import org.ebics.client.http.factory.PooledSimpleHttpClientFactory
import org.ebics.client.http.factory.HttpClientGlobalConfiguration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PooledHttpClientFactoryTest {
    @Test
    fun createSimpleHttpClientWithoutProxyAndSllTSFromFactory() {
        val configuration = object : HttpClientConfiguration {
            override val displayName: String = "default"
            override val sslTrustedStoreFile: String? = null
            override val sslTrustedStoreFilePassword: String? = null
            override val httpProxyHost: String? = null
            override val httpProxyPort: Int? = null
            override val httpProxyUser: String? = null
            override val httpProxyPassword: String? = null
            override val httpContentType: String = ""
            override val socketTimeoutMilliseconds: Int = 300000
            override val connectionTimeoutMilliseconds: Int = 300000
        }
        val poolConfig = object  : HttpClientGlobalConfiguration {
            override val connectionPoolMaxTotal = 5
            override val connectionPoolDefaultMaxPerRoute = 10
            override val configurations: Map<String, HttpClientConfiguration> = mapOf("default" to configuration)
        }
        val factory = PooledSimpleHttpClientFactory(poolConfig)
        val client = factory.getHttpClient("default")
        Assertions.assertNotNull(client)
    }

    @Test
    fun gettingDefaultClientWithoutProvidingSpecificConfiguration() {
        val poolConfig = object  : HttpClientGlobalConfiguration {
            override val connectionPoolMaxTotal = 5
            override val connectionPoolDefaultMaxPerRoute = 10
            override val configurations: Map<String, HttpClientConfiguration> = emptyMap()
        }
        val factory = PooledSimpleHttpClientFactory(poolConfig)
        val client = factory.getHttpClient("default")
        Assertions.assertNotNull(client)
    }

    @Test
    fun gettingClientFromFactoryForNonExistingConfiguration() {
        val configuration = object : HttpClientConfiguration {
            override val displayName: String = "default"
            override val sslTrustedStoreFile: String? = null
            override val sslTrustedStoreFilePassword: String? = null
            override val httpProxyHost: String? = null
            override val httpProxyPort: Int? = null
            override val httpProxyUser: String? = null
            override val httpProxyPassword: String? = null
            override val httpContentType: String = ""
            override val socketTimeoutMilliseconds: Int = 300000
            override val connectionTimeoutMilliseconds: Int = 300000
        }
        val poolConfig = object  : HttpClientGlobalConfiguration {
            override val connectionPoolMaxTotal = 5
            override val connectionPoolDefaultMaxPerRoute = 10
            override val configurations: Map<String, HttpClientConfiguration> = mapOf("default" to configuration)
        }
        val factory = PooledSimpleHttpClientFactory(poolConfig)
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            factory.getHttpClient("test")
        }
    }
}