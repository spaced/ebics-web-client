package org.ebics.client.http.factory

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.ebics.client.http.client.HttpClientConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "http.client")
//Restrict JSON exposed fields to selected class
// so the spring proxy parts like $$beanFactory are omitted
@JsonSerialize(`as` = HttpClientGlobalConfiguration::class)
class HttpClientGlobalConfiguration: IHttpClientGlobalConfiguration {
    override var connectionPoolMaxTotal: Int = 25
    override var connectionPoolDefaultMaxPerRoute: Int = 5
    override var configurations: Map<String, HttpClientConfiguration> = mapOf("default" to HttpClientConfiguration())
}