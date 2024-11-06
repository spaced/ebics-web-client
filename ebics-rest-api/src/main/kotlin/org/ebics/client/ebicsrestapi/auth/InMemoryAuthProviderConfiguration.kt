package org.ebics.client.ebicsrestapi.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication
import org.springframework.security.core.userdetails.User


@Profile("dev")
@Configuration
@EnableGlobalAuthentication
class InMemoryAuthProviderConfiguration {

    @Autowired
    fun configure(builder: AuthenticationManagerBuilder) {
        builder.inMemoryAuthentication()
            .withUser(User.withUsername("guest").password("{noop}pass").roles("GUEST").build())
            .withUser(User.withUsername("user").password("{noop}pass").roles("USER", "GUEST").build())
            .withUser(User.withUsername("admin").password("{noop}pass").roles("ADMIN", "USER", "GUEST").build())
    }
}
