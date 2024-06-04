package org.ebics.client.ebicsrestapi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import javax.sql.DataSource


@Configuration
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
class SecurityConfiguration() {

    @Bean
    fun configure(): InMemoryUserDetailsManager {
        return InMemoryUserDetailsManager(
            User.withUsername("guest").password("password").roles("GUEST").build(),
            User.withUsername("user").password("password").roles("USER", "GUEST").build(),
            User.withUsername("admin").password("password").roles("ADMIN", "USER", "GUEST").build()
        )
    }

    @Bean
    fun filterChainBasic(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize(HttpMethod.GET, "/bankconnections",hasAnyRole("ADMIN", "USER", "GUEST"))
                authorize(HttpMethod.POST, "/bankconnections/{\\d+}/H00{\\d+}/**",hasAnyRole("USER", "GUEST"))
                authorize(HttpMethod.GET, "/bankconnections/{\\d+}/H00{\\d+}/**",hasAnyRole("USER", "GUEST"))
                authorize(HttpMethod.POST, "/bankconnections",hasRole("USER"))
                authorize(HttpMethod.PUT, "/bankconnections/{\\d+}",hasRole("USER"))
                authorize(HttpMethod.DELETE, "/bankconnections/{\\d+}",hasAnyRole("ADMIN", "USER"))
                authorize(HttpMethod.GET, "/banks/**",hasAnyRole("ADMIN", "USER", "GUEST"))
                authorize(HttpMethod.POST, "/banks/**",hasRole("ADMIN"))
                authorize(HttpMethod.PUT, "/banks/**",hasRole("ADMIN"))
                authorize(HttpMethod.PATCH, "/banks/**",hasRole("ADMIN"))
                authorize(HttpMethod.DELETE, "/banks/**",hasRole("ADMIN"))
                authorize(HttpMethod.GET, "/user",hasAnyRole("ADMIN", "USER", "GUEST"))
                authorize(HttpMethod.GET, "/user/settings",hasAnyRole("ADMIN", "USER", "GUEST"))
                authorize(HttpMethod.PUT, "/user/settings",hasAnyRole("ADMIN", "USER", "GUEST"))
            }
            httpBasic { }
            cors {  }
            csrf { disable() }
            formLogin { disable() }
        }
        return http.build()
        //http.httpBasic().and().authorizeRequests().antMatchers("/users", "/").permitAll().anyRequest().authenticated()
    }
}
