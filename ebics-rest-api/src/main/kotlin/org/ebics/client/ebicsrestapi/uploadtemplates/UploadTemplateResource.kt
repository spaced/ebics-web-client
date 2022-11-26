package org.ebics.client.ebicsrestapi.uploadtemplates

import org.ebics.client.api.uploadtemplates.CreateOrUpdateFileTemplateRequest
import org.ebics.client.api.uploadtemplates.FileTemplate
import org.ebics.client.api.uploadtemplates.FileTemplateService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("filetemplate")
@CrossOrigin(origins = ["http://localhost:8081"])
class UploadTemplateResource(private val fileTemplateService: FileTemplateService) {
    @GetMapping
    fun listTemplates(): List<FileTemplate> = fileTemplateService.listAllTemplates()

    @GetMapping("{fileTemplateId}")
    fun getTemplateById(@PathVariable fileTemplateId: Long) = fileTemplateService.getTemplateById(fileTemplateId)

    @PostMapping
    fun createTemplate(@RequestBody fileTemplate: CreateOrUpdateFileTemplateRequest): Long = fileTemplateService.createTemplate(fileTemplate)

    @PutMapping("{fileTemplateId}")
    fun updateTemplate(@PathVariable fileTemplateId: Long, @RequestBody fileTemplate: CreateOrUpdateFileTemplateRequest) = fileTemplateService.updateTemplate(fileTemplateId, fileTemplate)

    @DeleteMapping("{fileTemplateId}")
    fun deleteTemplate(@PathVariable fileTemplateId: Long) = fileTemplateService.deleteTemplate(fileTemplateId)
}