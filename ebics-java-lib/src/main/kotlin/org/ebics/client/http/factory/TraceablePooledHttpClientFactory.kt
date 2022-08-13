package org.ebics.client.http.factory

import org.apache.http.impl.client.CloseableHttpClient
import org.ebics.client.api.trace.TraceManager
import org.ebics.client.api.trace.IBankConnectionTraceSession
import org.ebics.client.http.client.request.HttpClientRequest
import org.ebics.client.http.client.HttpClientRequestConfiguration
import org.ebics.client.http.client.TraceableHttpClient
import org.ebics.client.interfaces.ContentFactory
import org.ebics.client.io.ByteArrayContentFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class TraceablePooledHttpClientFactory(private val config: HttpClientGlobalConfiguration,
                                       private val traceManager: TraceManager) :
    AbstractPooledHttpClientFactory<TraceableHttpClient>(config),
    ITraceableHttpClientFactory<TraceableHttpClient> {

    override fun sendAndTraceRequest(
        httpTransferSession: IHttpTransferSession,
        traceSession: IBankConnectionTraceSession,
        contentFactory: ContentFactory
    ): ByteArrayContentFactory {
        return getHttpClient(httpTransferSession.httpConfigurationName).sendAndTrace(
            HttpClientRequest(
                httpTransferSession.ebicsUrl,
                contentFactory
            ), traceSession
        )
    }

    override fun instantiateHttpClient(
        httpClient: CloseableHttpClient,
        configuration: HttpClientRequestConfiguration,
        configurationName: String
    ): TraceableHttpClient = TraceableHttpClient(httpClient, configuration, configurationName, traceManager)
}