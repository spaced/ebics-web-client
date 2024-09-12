package org.ebics.client.ebicsrestapi.ldap


import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "spring.ldap.search")
data class LdapSearchProperties (
    val group: LdapSearchPattern,
    val user: LdapSearchPattern
)


data class LdapSearchPattern(
    val base: String = "",
    val filter: String
)
