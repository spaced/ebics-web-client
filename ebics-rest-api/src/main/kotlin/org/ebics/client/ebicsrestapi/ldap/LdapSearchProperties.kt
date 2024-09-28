package org.ebics.client.ebicsrestapi.ldap


import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "spring.ldap.search")
data class LdapSearchProperties (
    val domain: String = "",
    val group: LdapSearchPattern = LdapSearchPattern("","member={0}"),
    val user: LdapSearchPattern = LdapSearchPattern("","(uid={0})"),
    val mapping: Map<String,Array<String>>? // mapping of spring-role -> ldap-role
)


data class LdapSearchPattern(
    val base: String = "",
    val filter: String = ""
)
