package org.ebics.client.http

import org.ebics.client.http.factory.HttpClientGlobalConfiguration
import org.ebics.client.http.factory.PooledSimpleHttpClientFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [PooledSimpleHttpClientFactory::class, HttpClientGlobalConfiguration::class])
class PooledHttpClientFactoryTest(
    @Autowired private val httpClientFactory: PooledSimpleHttpClientFactory) {

    @Test
    fun getDefaultHttpClient() {
        val httpClient = httpClientFactory.getHttpClient("default")
        Assertions.assertNotNull(httpClient)
    }

    @Test
    fun gettingClientFromFactoryForNonExistingConfiguration() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            httpClientFactory.getHttpClient("test")
        }
    }
}