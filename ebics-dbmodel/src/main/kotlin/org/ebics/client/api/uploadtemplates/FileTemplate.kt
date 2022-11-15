package org.ebics.client.api.uploadtemplates

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.uploadtemplates.permission.FileTemplateAccessRightController
import javax.persistence.*

@Entity
data class FileTemplate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Lob
    val fileContentText: String,
    val templateName: String,
    val templateTags: String,

    //False => out of the box template (source code maintained)
    //True => custom template from DB
    @Transient
    var custom: Boolean,

    val creatorUserId: String,
    val shared: Boolean,
) : FileTemplateAccessRightController {
    companion object {
        fun from(
            createOrUpdateFileTemplateRequest: CreateOrUpdateFileTemplateRequest,
            authCtx: AuthenticationContext
        ): FileTemplate {
            return FileTemplate(
                createOrUpdateFileTemplateRequest.id,
                createOrUpdateFileTemplateRequest.fileContentText,
                createOrUpdateFileTemplateRequest.templateName,
                createOrUpdateFileTemplateRequest.templateTags,
                true,
                authCtx.name,
                createOrUpdateFileTemplateRequest.shared,
            )
        }
    }

    @JsonIgnore
    override fun getCreatorName(): String = creatorUserId
    @JsonIgnore
    override fun isShared(): Boolean = shared
    @JsonIgnore
    override fun getObjectName(): String = "File Template: '$templateName' created by: '$creatorUserId'"
}
