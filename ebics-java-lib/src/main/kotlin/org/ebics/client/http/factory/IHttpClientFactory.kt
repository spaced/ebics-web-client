package org.ebics.client.http.factory

import org.apache.http.impl.client.CloseableHttpClient
import org.ebics.client.http.client.HttpClient
import org.ebics.client.http.client.HttpClientRequestConfiguration
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory

interface IHttpClientFactory<T : HttpClient> {
    /**
     * Get preconfigured HTTP reusable client
     */
    fun getHttpClient(configurationName: String): T

    fun instantiateHttpClient(
        httpClient: CloseableHttpClient,
        configuration: HttpClientRequestConfiguration,
        configurationName: String
    ): T

    fun sendRequest(httpTransferSession: IHttpTransferSession, contentFactory: ContentFactory): ByteArrayContentFactory
}