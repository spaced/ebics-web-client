package org.ebics.client.ebicsrestapi

import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher


@Configuration
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
class SecurityConfiguration() {


    @Bean
    fun filterChainBasic(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize(HttpMethod.GET, "/bankconnections",hasAnyRole("ADMIN", "USER", "GUEST"))
                authorize(AntPathRequestMatcher( "/bankconnections/{\\d+}/H00{\\d+}/**",HttpMethod.POST.name()),hasAnyRole("USER", "GUEST"))
                authorize(AntPathRequestMatcher("/bankconnections/{\\d+}/H00{\\d+}/**", HttpMethod.GET.name()),hasAnyRole("USER", "GUEST"))
                authorize(HttpMethod.POST, "/bankconnections",hasRole("USER"))
                authorize(AntPathRequestMatcher("/bankconnections/{\\d+}", HttpMethod.PUT.name()),hasRole("USER"))
                authorize(AntPathRequestMatcher("/bankconnections/{\\d+}",HttpMethod.DELETE.name()),hasAnyRole("ADMIN", "USER"))
                authorize(HttpMethod.GET, "/banks/**",hasAnyRole("ADMIN", "USER", "GUEST"))
                authorize(HttpMethod.POST, "/banks/**",hasRole("ADMIN"))
                authorize(HttpMethod.PUT, "/banks/**",hasRole("ADMIN"))
                authorize(HttpMethod.PATCH, "/banks/**",hasRole("ADMIN"))
                authorize(HttpMethod.DELETE, "/banks/**",hasRole("ADMIN"))
                authorize(HttpMethod.GET, "/user",hasAnyRole("ADMIN", "USER", "GUEST"))
                authorize(HttpMethod.GET, "/user/settings",hasAnyRole("ADMIN", "USER", "GUEST"))
                authorize(HttpMethod.PUT, "/user/settings",hasAnyRole("ADMIN", "USER", "GUEST"))
            }
            cors { }
            csrf { disable() }
            formLogin { }
        }
        return http.build()
    }
}
