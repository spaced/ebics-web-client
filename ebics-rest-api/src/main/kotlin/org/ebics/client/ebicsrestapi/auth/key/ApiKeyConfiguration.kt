package org.ebics.client.ebicsrestapi.auth.key

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@Configuration
@EnableConfigurationProperties(ApiKeyProperties::class)
@ConditionalOnProperty("ebics.api.enabled", havingValue = "true")
@EnableGlobalAuthentication
class ApiKeyConfiguration {

    @Autowired
    fun configure(builder: AuthenticationManagerBuilder, apiKeyProperties: ApiKeyProperties) {
        builder.authenticationProvider(ApiKeyAuthenticationProvider(apiKeyProperties.clients))
    }

}