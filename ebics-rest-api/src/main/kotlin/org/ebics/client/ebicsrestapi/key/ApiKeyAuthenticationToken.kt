package org.ebics.client.ebicsrestapi.key

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils

/**
 * An api key pair authentication
 */
class ApiKeyAuthenticationToken(val appId: String, val apiKey: String, authorities: Collection<out GrantedAuthority?> = AuthorityUtils.NO_AUTHORITIES) : AbstractAuthenticationToken(authorities) {


    override fun getPrincipal(): Any? {
        return appId
    }

    override fun getCredentials(): Any? {
        return apiKey
    }

    override fun eraseCredentials() {
        super.eraseCredentials()
    }
}