package org.ebics.client.ebicsrestapi.ldap


import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.ldap.core.support.BaseLdapPathContextSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator
import java.util.*

typealias AuthorityRecord = Map<String, List<String>>
typealias AuthorityMapper = (AuthorityRecord) -> GrantedAuthority?

@Configuration
@Profile("!dev")
@EnableConfigurationProperties(LdapSearchProperties::class)
class LdapConfiguration {
    @Bean
    fun authorities(contextSource: BaseLdapPathContextSource, searchProperties: LdapSearchProperties): LdapAuthoritiesPopulator {
        val authorities = DefaultLdapAuthoritiesPopulator(contextSource, searchProperties.group.base)
        authorities.setGroupSearchFilter(searchProperties.group.filter)
        val mapper: AuthorityMapper = { record ->
            val roles = record["cn"]
            val role = roles?.first()
            val mappedRole= searchProperties.mapping?.get(role)?:role
            mappedRole?.let{ SimpleGrantedAuthority("ROLE_${mappedRole.uppercase()}") }
        }

        authorities.setAuthorityMapper( mapper)
        return authorities
    }

    @Bean
    fun authenticationManager(contextSource: BaseLdapPathContextSource,
                              authorities: LdapAuthoritiesPopulator,
                              searchProperties: LdapSearchProperties
    ): AuthenticationManager {
        val factory = LdapBindAuthenticationManagerFactory(contextSource)
        factory.setUserSearchFilter(searchProperties.user.filter)
        factory.setUserSearchBase(searchProperties.user.base)
        factory.setLdapAuthoritiesPopulator(authorities)
        return factory.createAuthenticationManager()
    }

}