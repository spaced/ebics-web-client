package org.ebics.client.ebicsrestapi.ldap

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.ldap.LdapProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.ldap.core.support.BaseLdapPathContextSource
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider
import org.springframework.security.ldap.authentication.BindAuthenticator
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator

typealias AuthorityRecord = Map<String, List<String>>
typealias AuthorityMapper = (AuthorityRecord) -> GrantedAuthority?

@Configuration
@EnableConfigurationProperties(LdapSearchProperties::class)
@ConditionalOnProperty("ebics.auth.ldap", havingValue = "true")
class LdapConfiguration {

    @Bean
    @ConditionalOnProperty("spring.ldap.search.group.base")
    fun authorities(contextSource: BaseLdapPathContextSource, searchProperties: LdapSearchProperties): LdapAuthoritiesPopulator {
        val authorities = DefaultLdapAuthoritiesPopulator(contextSource, searchProperties.group.base)
        authorities.setGroupSearchFilter(searchProperties.group.filter)
        val mapper: AuthorityMapper = { record ->
            val roles = record["cn"]
            val role = roles?.first()
            val mappedRole = searchProperties.mapping?.get(role)
            mappedRole?.first()?.let { r -> SimpleGrantedAuthority("ROLE_${r.uppercase()}") }
        }

        authorities.setAuthorityMapper(mapper)
        return authorities
    }

    @Bean
    @ConditionalOnProperty("spring.ldap.search.use-member-of-attribute", havingValue = "true")
    fun activeDirectoryAuthorities(searchProperties: LdapSearchProperties): LdapAuthoritiesPopulator {
        return ActiveDirectoryRoleMapperPopulator(searchProperties.mapping)
    }

    @Bean
    @ConditionalOnProperty("spring.ldap.search.user.base")
    fun authenticationBindProvider(contextSource: BaseLdapPathContextSource, authorities: LdapAuthoritiesPopulator, searchProperties: LdapSearchProperties): LdapAuthenticationProvider {
        val ba = BindAuthenticator(contextSource)
        ba.setUserSearch(FilterBasedLdapUserSearch(searchProperties.user.base, searchProperties.user.filter, contextSource))
        return LdapAuthenticationProvider(ba, authorities)
    }

    @Bean
    @ConditionalOnProperty("spring.ldap.search.domain")
    fun authenticationADProvider(ldapProperties: LdapProperties, searchProperties: LdapSearchProperties, authorities: LdapAuthoritiesPopulator): ActiveDirectoryLdapAuthenticationProvider {
        val adProvider = ActiveDirectoryLdapAuthenticationProvider(searchProperties.domain, ldapProperties.urls[0], ldapProperties.base)
        adProvider.setAuthoritiesPopulator(authorities)
        return adProvider
    }

    @Autowired
    fun configure(builder: AuthenticationManagerBuilder, ldapAuth: AbstractLdapAuthenticationProvider) {
        builder.authenticationProvider(ldapAuth)
    }

}