package org.ebics.client.ebicsrestapi.key

import org.ebics.client.ebicsrestapi.key.ApiKeyProperties.ApiKey
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

class ApiKeyAuthenticationProvider(val appIds: Map<String, ApiKey>) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val auth = authentication as ApiKeyAuthenticationToken
        val v = appIds[auth.principal]
        if (v == null || v.key != authentication.credentials) throw BadCredentialsException("bad api id or key")

        val role = SimpleGrantedAuthority("ROLE_${v.role.uppercase()}")
        return ApiKeyAuthenticationToken(auth.appId, auth.apiKey, listOf(role))
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication.isAssignableFrom(ApiKeyAuthenticationToken::class.java)
    }
}