package org.ebics.client.api.uploadtemplates

import org.ebics.client.api.NotFoundException
import org.ebics.client.api.security.AuthenticationContext
import org.springframework.stereotype.Service

@Service
class FileTemplateService(private val fileTemplateRepository: FileTemplateRepository) {
    private val systemUserId: String = "system"

    fun listAll(): List<FileTemplate> {
        return fileTemplateRepository.findAll()
            .map { template ->
                template.custom = true
                template
            } + listOwnTemplates()
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

    private fun listOwnTemplates(): List<FileTemplate> {
        return listOf(
            FileTemplate(
                -100L, "test template content SEPA", "Pain.001.001.09.ch.02.xml SPS SEPA Payment",
                "Payment,SEPA,SPS,CH", false, systemUserId, true
            ),
            FileTemplate(
                -101L, "test template content2 QRR", "Pain.001.001.09.ch.02.xml SPS Domestic QRR Payment",
                "Payment,QRR,SPS,CH", false, systemUserId, true
            )
        )
    }
}