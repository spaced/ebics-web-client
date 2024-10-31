package org.ebics.client.ebicsrestapi.key

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder

@Configuration
@EnableConfigurationProperties(ApiKeyProperties::class)
@ConditionalOnProperty("ebics.auth.api", havingValue = "true")
class ApiKeyConfiguration {

    @Autowired
    fun configure(builder: AuthenticationManagerBuilder, apiKeyProperties: ApiKeyProperties) {
        builder.authenticationProvider(ApiKeyAuthenticationProvider(apiKeyProperties.clients))
    }

}