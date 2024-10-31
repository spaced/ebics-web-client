package org.ebics.client.ebicsrestapi.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@Configuration
@Profile("dev")
class InMemoryAuthProviderConfiguration {

    @Autowired
    fun configure(builder: AuthenticationManagerBuilder) {
        val um = InMemoryUserDetailsManager(
            User.withUsername("guest").password("{noop}pass").roles("GUEST").build(),
            User.withUsername("user").password("{noop}pass").roles("USER", "GUEST").build(),
            User.withUsername("admin").password("{noop}pass").roles("ADMIN", "USER", "GUEST").build()
        )
        val d = DaoAuthenticationProvider()
        d.setUserDetailsService(um)
        builder.authenticationProvider(d)
    }
}
