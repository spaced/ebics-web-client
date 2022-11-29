package org.ebics.client.api.uploadtemplates.permission

import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.security.BusinessRole
import org.ebics.client.api.security.ReadAccessRightsController
import org.ebics.client.api.security.WriteAccessRightsController
import org.slf4j.LoggerFactory

interface FileTemplateAccessRightController : ReadAccessRightsController, WriteAccessRightsController {
    /**
     * Indicates who created the template (userid)
     */
    fun getCreatorName(): String

    /**
     * Indicates shared template
     */
    fun isShared(): Boolean

    override fun hasReadAccess(authCtx: AuthenticationContext): Boolean {
        with(authCtx) {
            return when {
                authCtx.hasRole(BusinessRole.ROLE_ADMIN) -> {
                    logger.debug("Read permission for '{}' granted through admin role for '{}'", getObjectName(), authCtx.name)
                    true
                }
                authCtx.hasRole(BusinessRole.ROLE_USER) && getCreatorName() == name -> {
                    logger.debug("Read permission for '{}' granted through user access for '{}'", getObjectName(), authCtx.name)
                    true
                }
                authCtx.hasRole(BusinessRole.ROLE_GUEST) && isShared() -> {
                    logger.debug("Read permission for '{}' granted through guest access for '{}'", getObjectName(), authCtx.name)
                    true
                }
                else -> {
                    logger.debug(
                        "Read permission for '{}' denied, no role available '{}' for '{}'",
                        getObjectName(),
                        authorities.joinToString(),
                        authCtx.name
                    )
                    false
                }
            }
        }
    }

    override fun hasWriteAccess(authCtx: AuthenticationContext): Boolean {
        with(authCtx) {
            return when {
                authCtx.hasRole(BusinessRole.ROLE_ADMIN) -> {
                    logger.debug("Write permission for '{}' granted through admin role for '{}'", getObjectName(), authCtx.name)
                    true
                }
                authCtx.hasRole(BusinessRole.ROLE_USER) && getCreatorName() == name -> {
                    logger.debug("Write permission for '{}' granted through user access for '{}'", getObjectName(), authCtx.name)
                    true
                }
                else -> {
                    logger.debug(
                        "Write permission for '{}' denied, no role available '{}' for '{}'",
                        getObjectName(),
                        authorities.joinToString(),
                        authCtx.name
                    )
                    false
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FileTemplateAccessRightController::class.java)
    }
}