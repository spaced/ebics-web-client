package org.ebics.client.ebicsrestapi.auth.ldap

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider

@Configuration
@ConditionalOnProperty("spring.ldap.enabled", havingValue = "true")
@EnableGlobalAuthentication
class AuthConfiguration {

     @Autowired
    fun configure(builder: AuthenticationManagerBuilder, ldapAuth: AbstractLdapAuthenticationProvider) {
        builder.authenticationProvider(ldapAuth)
    }
}