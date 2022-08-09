package org.ebics.client.http.factory

import org.apache.http.impl.client.CloseableHttpClient
import org.ebics.client.http.client.HttpClientRequestConfiguration
import org.ebics.client.http.client.SimpleHttpClient

class PooledSimpleHttpClientFactory(config: HttpClientGlobalConfiguration) :
    AbstractPooledHttpClientFactory<SimpleHttpClient>(config) {
    override fun instantiateHttpClient(
        httpClient: CloseableHttpClient,
        configuration: HttpClientRequestConfiguration,
        configurationName: String
    ): SimpleHttpClient {
        return SimpleHttpClient(httpClient, configuration, configurationName)
    }
}