package org.ebics.client.ebicsrestapi.ldap


import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "spring.ldap.search")
data class LdapSearchProperties (
    val group: LdapSearchPattern,
    val user: LdapSearchPattern,
    val mapping: Map<String,String>? // mapping of spring-role -> ldap-role
)


data class LdapSearchPattern(
    val base: String = "",
    val filter: String
)
