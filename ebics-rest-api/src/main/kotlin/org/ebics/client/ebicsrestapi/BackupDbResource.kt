package org.ebics.client.ebicsrestapi

import jakarta.persistence.EntityManager
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.io.File

@RestController
@RequestMapping("backup")
@CrossOrigin(origins = ["http://localhost:8081"])
class BackupDbResource(private val em: EntityManager) {

    @GetMapping
    @Transactional
    fun createBackup(): ResponseEntity<Resource> {

        //generate a random backup file
        val backupFile = File.createTempFile("backup","zip")
        backupFile.deleteOnExit()

        //create backup
        val d = em.createNativeQuery(String.format("BACKUP TO '%s';",backupFile.absolutePath))
        d.executeUpdate()

        // take the risk to load in memory, so we can delete the file
        // better would be URIResource(backupFile.toURI) to read directly
        // and after response postpone delete backup file
        val backupResource = ByteArrayResource(backupFile.readBytes())
        backupFile.delete()

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(backupResource)
    }
}

