package org.ebics.client.api.uploadtemplates

import org.ebics.client.api.NotFoundException
import org.ebics.client.api.getById
import org.ebics.client.api.security.AuthenticationContext
import org.springframework.stereotype.Service

@Service
class FileTemplateService(private val fileTemplateRepository: FileTemplateRepository) {
    private val systemUserId: String = "system"

    fun listAllTemplates(): List<FileTemplate> {
        val authCtx = AuthenticationContext.fromSecurityContext()
        val templates = fileTemplateRepository.findAll()
            .map { template ->
                template.custom = true
                template.canBeEdited = template.hasWriteAccess(authCtx)
                template
            }.filter { it.hasReadAccess(authCtx) } + getPredefinedResourceTemplates()
        return templates.sortedBy { it.templateName }
    }

    fun getTemplateById(templateId: Long): FileTemplate {
        val predefinedTemplate = getPredefinedResourceTemplates().find { template -> template.id == templateId }
        return if (predefinedTemplate != null)
            predefinedTemplate
        else {
            val authCtx = AuthenticationContext.fromSecurityContext()
            fileTemplateRepository.getById(templateId, "file template").apply {
                checkReadAccess(authCtx)
                canBeEdited = hasWriteAccess(authCtx)
            }
        }
    }

    fun updateTemplate(fileTemplateId: Long, fileTemplateRequest: CreateOrUpdateFileTemplateRequest): Long {
        val authCtx = AuthenticationContext.fromSecurityContext()
        val updatedFileTemplate = FileTemplate.from(fileTemplateRequest, authCtx)
        require(fileTemplateId == updatedFileTemplate.id) { "Update of template failed, provided inconsistent arguments, the id doesn't match, $fileTemplateId is not equal to ${updatedFileTemplate.id}" }
        val currentTemplate = getTemplateByIdSafe(fileTemplateId)
        currentTemplate.checkWriteAccess(authCtx)
        checkToPreventSystemTemplatesToBeModified(currentTemplate)
        fileTemplateRepository.save(updatedFileTemplate)
        return updatedFileTemplate.id
    }

    fun createTemplate(fileTemplateRequest: CreateOrUpdateFileTemplateRequest): Long {
        val authCtx = AuthenticationContext.fromSecurityContext()
        val fileTemplate = FileTemplate.from(fileTemplateRequest, authCtx)
        require(fileTemplate.id == null) { "Creating of template failed, provided inconsistent arguments, the id must be null but is ${fileTemplate.id}" }
        fileTemplate.checkWriteAccess(authCtx)
        checkToPreventSystemTemplatesToBeModified(fileTemplate)
        fileTemplateRepository.save(fileTemplate)
        return fileTemplate.id!!
    }

    fun deleteTemplate(fileTemplateId: Long) {
        val authCtx = AuthenticationContext.fromSecurityContext()
        val currentTemplate = getTemplateByIdSafe(fileTemplateId)
        currentTemplate.checkWriteAccess(authCtx)
        checkToPreventSystemTemplatesToBeModified(currentTemplate)
        fileTemplateRepository.deleteById(fileTemplateId)
    }

    private fun getTemplateByIdSafe(fileTemplateId: Long): FileTemplate {
        val fileTemplate = fileTemplateRepository.findById(fileTemplateId)
        if (fileTemplate.isPresent) {
            return fileTemplate.get()
        } else {
            throw NotFoundException(fileTemplateId, "fileTemplate", null)
        }
    }

    private fun checkToPreventSystemTemplatesToBeModified(fileTemplateId: Long) {
        checkToPreventSystemTemplatesToBeModified(getTemplateByIdSafe(fileTemplateId))
    }

    private fun checkToPreventSystemTemplatesToBeModified(fileTemplate: FileTemplate) {
        if (fileTemplate.creatorUserId == systemUserId)
            throw IllegalArgumentException("The system default templates can't be modified")
    }

    private fun getPredefinedResourceTemplates(): List<FileTemplate> {
        return listOf(
            FileTemplate(
                -101L,
                getResourceFileContent("/upload-templates/pain.001.001.09_PaymentType_Domestic_QRR.xml"),
                "Pain.001.001.09.ch.02.xml SPS Domestic QRR Payment",
                "Payment,QRR,SPS,CH,Domestic,ISOv2019",
                FileFormatType.Xml,
                custom = false,
                canBeEdited = false,
                creatorUserId = systemUserId,
                guestAccess = true
            ),
            FileTemplate(
                -102L,
                getResourceFileContent("/upload-templates/pain.001.001.09_PaymentType_Domestic_SCOR.xml"),
                "Pain.001.001.09.ch.02.xml SPS Domestic SCOR Payment",
                "Payment,SCOR,SPS,CH,Domestic,ISOv2019",
                FileFormatType.Xml,
                custom = false,
                canBeEdited = false,
                creatorUserId = systemUserId,
                guestAccess = true
            ),
            FileTemplate(
                -102L,
                getResourceFileContent("/upload-templates/pain.001.001.09_PaymentType_SEPA.xml"),
                "Pain.001.001.09.ch.02.xml SPS SEPA Payment",
                "Payment,SEPA,SPS,CH,ISOv2019",
                FileFormatType.Xml,
                custom = false,
                canBeEdited = false,
                creatorUserId = systemUserId,
                guestAccess = true
            ),
            FileTemplate(
                -103L,
                getResourceFileContent("/upload-templates/pain.001.001.09_PaymentType_XBorder-V1.xml"),
                "Pain.001.001.09.ch.02.xml SPS X-Border V1 Payment",
                "Payment,SWIFT,SPS,CH,ISOv2019",
                FileFormatType.Xml,
                custom = false,
                canBeEdited = false,
                creatorUserId = systemUserId,
                guestAccess = true
            ),
            FileTemplate(
                -104L,
                getResourceFileContent("/upload-templates/pain.001.001.09_PaymentType_XBorder-V2.xml"),
                "Pain.001.001.09.ch.02.xml SPS X-Border V2 Payment",
                "Payment,SWIFT,SPS,CH,ISOv2019",
                FileFormatType.Xml,
                custom = false,
                canBeEdited = false,
                creatorUserId = systemUserId,
                guestAccess = true
            ),
            FileTemplate(
                -105L,
                getResourceFileContent("/upload-templates/pain.001.001.09-2B-3C.xml"),
                "Pain.001.001.09.ch.02.xml SPS 2xB 3xC Payment",
                "Payment,SPS,CH,Multiple-B-Level,Multiple-C-Level,ISOv2019",
                FileFormatType.Xml,
                custom = false,
                canBeEdited = false,
                creatorUserId = systemUserId,
                guestAccess = true
            ),
            FileTemplate(
                -110L,
                getResourceFileContent("/upload-templates/pain.001.001.03.ch.02-2B-3C.xml"),
                "pain.001.001.03.ch.xml SPS Payment 2xB 3xC",
                "Payment,QRR,SPS,CH,Multiple-B-Level,Multiple-C-Level,ISOv2009",
                FileFormatType.Xml,
                custom = false,
                canBeEdited = false,
                systemUserId,
                true
            ),
            FileTemplate(
                -111L,
                getResourceFileContent("/upload-templates/pain.001.001.03.ch.02-PaymentType_Domestic_CH01.xml"),
                "pain.001.001.03.ch.xml SPS Payment CH01 ESR",
                "Payment,QRR,SPS,CH,CH01,ESR,Domestic,ISOv2009",
                FileFormatType.Xml,
                custom = false,
                canBeEdited = false,
                systemUserId,
                true
            ),
            FileTemplate(
                -112L,
                getResourceFileContent("/upload-templates/pain.001.001.03.ch.02-PaymentType_Domestic_CH02.xml"),
                "pain.001.001.03.ch.xml SPS Payment CH02 ESR",
                "Payment,QRR,SPS,CH,CH02,ESR,Domestic,ISOv2009",
                FileFormatType.Xml,
                custom = false,
                canBeEdited = false,
                systemUserId,
                true
            ),
            FileTemplate(
                -113L,
                getResourceFileContent("/upload-templates/pain.001.001.03.ch.02-PaymentType_SEPA.xml"),
                "pain.001.001.03.ch.xml SPS Payment SEPA",
                "Payment,QRR,SPS,CH,SEPA,ISOv2009",
                FileFormatType.Xml,
                custom = false,
                canBeEdited = false,
                systemUserId,
                true
            ),
        )
    }

    private fun getResourceFileContent(resourceName: String): String {
        val resFileUrl = requireNotNull(FileTemplateService::class.java.getResource(resourceName))
        { "The statically defined resource path '$resourceName' is not available as file" }
        return resFileUrl.readText(Charsets.UTF_8)
    }
}