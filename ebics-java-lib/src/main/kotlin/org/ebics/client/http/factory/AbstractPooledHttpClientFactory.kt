package org.ebics.client.http.factory

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.ProxyAuthenticationStrategy
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.ssl.SSLContexts
import org.ebics.client.http.*
import org.ebics.client.http.client.HttpClient
import org.ebics.client.http.client.HttpClientConfiguration
import org.ebics.client.http.client.request.HttpClientRequest
import org.ebics.client.http.client.HttpClientRequestConfiguration
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.utils.requireNotNullAndNotBlank
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


abstract class AbstractPooledHttpClientFactory<T: HttpClient>(
    private val config: HttpClientGlobalConfiguration
) : IHttpClientFactory<T> {

    private val poolingConnManager = PoolingHttpClientConnectionManager().apply {
        maxTotal = config.connectionPoolMaxTotal
        defaultMaxPerRoute = config.connectionPoolDefaultMaxPerRoute
    }

    private lateinit var httpClients: Map<String, T>

    @PostConstruct
    fun initConfigurations() {
        val configurations = config.configurations.ifEmpty {
            mapOf("default" to object : HttpClientConfiguration {
                override val displayName: String = "default"
                override val sslTrustedStoreFile: String? = null
                override val sslTrustedStoreFilePassword: String? = null
                override val httpProxyHost: String? = null
                override val httpProxyPort: Int? = null
                override val httpProxyUser: String? = null
                override val httpProxyPassword: String? = null
                override val httpContentType: String = "text/xml; charset=ISO-8859-1"
                override val socketTimeoutMilliseconds: Int = 300 * 1000
                override val connectionTimeoutMilliseconds: Int = 300 * 1000
            })
        }
        httpClients = createHttpClients(configurations)
    }

    @PreDestroy
    fun preDestroy() {
        logger.info("Closing gracefully the HTTP clients and connection manager")
        httpClients.forEach { (_, client) -> client.close() }
        poolingConnManager.close()
    }

    override fun getHttpClient(configurationName: String): T {
        return requireNotNull(httpClients[configurationName]) {
            "Requested HTTP configuration: '$configurationName' doesn't existing, following configuration names are available: ${httpClients.keys}"
        }
    }

    override fun sendRequest(
        httpTransferSession: IHttpTransferSession,
        contentFactory: ContentFactory
    ): ByteArrayContentFactory {
        return getHttpClient(httpTransferSession.httpConfigurationName).send(
            HttpClientRequest(
                httpTransferSession.ebicsUrl,
                contentFactory
            )
        )
    }

    private fun createHttpClients(namedClientConfigurations: Map<String, HttpClientConfiguration>): Map<String, T> {
        return namedClientConfigurations.map { config ->
            config.key to instantiateHttpClient(
                createHttpClient(config.key, config.value),
                config.value,
                config.key
            )
        }.toMap()
    }

    /**
     * Create Apache CloseableHttpClient client from EBICS configuration
     */
    private fun createHttpClient(
        configurationName: String,
        configuration: HttpClientConfiguration
    ): CloseableHttpClient {
        with(configuration) {
            val logPrefix = "HttpClient '$configurationName:${configuration.displayName}'"
            logger.info("Creating $logPrefix")
            with(HttpClientBuilder.create()) {
                with(RequestConfig.copy(RequestConfig.DEFAULT)) {
                    setSocketTimeout(socketTimeoutMilliseconds)
                    setConnectTimeout(connectionTimeoutMilliseconds)
                    if (!httpProxyHost.isNullOrBlank() && httpProxyPort != null) {
                        logger.debug("$logPrefix : setting proxy host:port=$httpProxyHost:$httpProxyPort")
                        setProxy(HttpHost(httpProxyHost!!, httpProxyPort!!))
                    } else {
                        logger.debug("$logPrefix : no proxy used")
                    }
                    setDefaultRequestConfig(build())
                }

                if (!httpProxyUser.isNullOrBlank()) {
                    logger.debug("$logPrefix : setting proxy credentials: $httpProxyUser:$httpProxyPassword")
                    val credentialsProvider = BasicCredentialsProvider().apply {
                        setCredentials(
                            AuthScope(httpProxyHost, httpProxyPort!!),
                            UsernamePasswordCredentials(httpProxyUser, httpProxyPassword)
                        )
                    }
                    setDefaultCredentialsProvider(credentialsProvider)
                    setProxyAuthenticationStrategy(ProxyAuthenticationStrategy())
                } else {
                    logger.debug("$logPrefix : no proxy credentials used")
                }

                if (!sslTrustedStoreFile.isNullOrBlank()) {
                    val sslTrustStoreFile = requireNotNullAndNotBlank(sslTrustedStoreFile)
                    logger.debug("$logPrefix : setting SSL trust store: $sslTrustStoreFile, pass: $sslTrustedStoreFilePassword")
                    val trustStoreFile = File(sslTrustStoreFile)
                    if (trustStoreFile.exists()) {
                        try {
                            val sslContextBuilder = SSLContexts.custom()
                            val sslContext =
                                if (sslTrustedStoreFilePassword != null)
                                    sslContextBuilder
                                        .loadTrustMaterial(trustStoreFile, sslTrustedStoreFilePassword!!.toCharArray())
                                else
                                    sslContextBuilder.loadTrustMaterial(trustStoreFile)
                            setSSLContext(sslContext.build())
                        } catch (e: Exception) {
                            logger.error(
                                "Error loading truststore file: '$sslTrustStoreFile' with password: '$sslTrustedStoreFilePassword' ",
                                e
                            )
                        }
                    } else
                        logger.error("Provided truststore file name doesn't exist: '$sslTrustStoreFile'")
                } else {
                    logger.debug("$logPrefix : no truststore file used")
                }
                setConnectionManager(poolingConnManager)
                setConnectionManagerShared(true)
                return build()
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractPooledHttpClientFactory::class.java)
    }
}