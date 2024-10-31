package org.ebics.client.ebicsrestapi.auth.ldap

import org.slf4j.LoggerFactory
import org.springframework.ldap.core.DirContextOperations
import org.springframework.ldap.core.DistinguishedName
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator


/**
 * Translates ad memberOf attribute to role based on ldap search property [LdapSearchProperties.mapping]
 * inspired by [DefaultActiveDirectoryAuthoritiesPopulator]
 */
class ActiveDirectoryRoleMapperPopulator(private val mapping: Map<String,Array<String>>?) : LdapAuthoritiesPopulator {
    private val logger = LoggerFactory.getLogger(ActiveDirectoryRoleMapperPopulator::class.java)
    override fun getGrantedAuthorities(
        userData: DirContextOperations?,
        username: String?
    ): Collection<GrantedAuthority?>? {
        val groups = userData?.getStringAttributes("memberOf")
        if (groups == null) {
            logger.debug("No values for 'memberOf' attribute.");
            return AuthorityUtils.NO_AUTHORITIES;
        }
        if (logger.isDebugEnabled) logger.debug("'memberOf' attribute values: " + groups.asList());

        return buildList {
            for (group in groups) {
                val mappedRoles = mapping?.get(DistinguishedName(group).removeLast().value)
                mappedRoles?.forEach{ r:String -> add(SimpleGrantedAuthority("ROLE_${r.uppercase()}"))}
            }
        }
    }

}