package org.ebics.client.ebicsrestapi.auth

import org.ebics.client.ebicsrestapi.auth.key.ApiKeyAuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("auth/types")
@CrossOrigin(origins = ["http://localhost:8081"])
class AuthProvidersResource(private val authenticationConf: AuthenticationConfiguration) {

    @GetMapping
    fun getAuthTypes(): List<String> {
        val am = authenticationConf.authenticationManager
        if (am !is ProviderManager) return emptyList()
        return am.providers.map { p ->
            when (p) {
                is ApiKeyAuthenticationProvider -> "key"
                is AbstractLdapAuthenticationProvider -> "server"
                is AbstractUserDetailsAuthenticationProvider -> "basic"
                else -> "unknown"
            }
        }
    }
}

