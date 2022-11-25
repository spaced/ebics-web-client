package org.ebics.client.api.uploadtemplates

class CreateOrUpdateFileTemplateRequest (
    val id:Long? = null,

    val fileContentText: String,
    val templateName: String,
    val templateTags: String,
    val fileFormat: FileFormatType,

    val shared: Boolean,
)