package org.ebics.client.api.uploadtemplates

import org.springframework.data.jpa.repository.JpaRepository

interface FileTemplateRepository : JpaRepository<FileTemplate, Long>