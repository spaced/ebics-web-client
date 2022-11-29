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
    @Enumerated(EnumType.STRING)
    val fileFormat: FileFormatType,

    //False => out of the box template (source code maintained)
    //True => custom template from DB
    @Transient
    var custom: Boolean,

    @Transient
    var canBeEdited: Boolean,

    val creatorUserId: String,

    val guestAccess: Boolean,
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
                createOrUpdateFileTemplateRequest.fileFormat,
                custom = true,
                canBeEdited = false,
                authCtx.name,
                createOrUpdateFileTemplateRequest.shared,
            )
        }
    }

    @JsonIgnore
    override fun getCreatorName(): String = creatorUserId
    @JsonIgnore
    override fun isShared(): Boolean = guestAccess
    @JsonIgnore
    override fun getObjectName(): String = "File Template: '$templateName' created by: '$creatorUserId'"
}
