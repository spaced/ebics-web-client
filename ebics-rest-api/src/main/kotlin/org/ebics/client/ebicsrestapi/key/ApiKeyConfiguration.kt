package org.ebics.client.ebicsrestapi.key

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ApiKeyProperties::class)
class ApiKeyConfiguration {

    @Bean
    fun apiKeyAuthentication(apiKeyProperties: ApiKeyProperties): ApiKeyAuthenticationProvider {
        return ApiKeyAuthenticationProvider(apiKeyProperties.clients)
    }

}