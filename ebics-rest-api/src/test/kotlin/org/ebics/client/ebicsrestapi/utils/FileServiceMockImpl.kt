package org.ebics.client.ebicsrestapi.utils

import org.ebics.client.api.trace.IFileService
import org.ebics.client.api.trace.TraceEntry
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsVersion
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class FileServiceMockImpl : IFileService {
    var fileContent: ByteArray? = null

    override fun getLastDownloadedFile(
        orderType: OrderTypeDefinition,
        user: BankConnectionEntity,
        ebicsVersion: EbicsVersion,
        useSharedPartnerData: Boolean
    ): TraceEntry {
        if (fileContent == null)
            throw NoSuchElementException("Mock: No value cached yet")
        else
            return TraceEntry(1, "${String(fileContent!!)}-cached", null, user, null,"1",
            "CCCX", ebicsVersion, upload = false, request = false, creator = "jan", orderType = orderType)
    }

    override fun addFile(
        user: BankConnectionEntity,
        orderType: OrderTypeDefinition,
        fileContent: ByteArray,
        sessionId: String,
        orderNumber: String?,
        ebicsVersion: EbicsVersion,
        upload: Boolean,
        request: Boolean
    ) {
        this.fileContent = fileContent
    }

    fun removeAllFilesOlderThan(dateTime: ZonedDateTime) {
        fileContent = null
    }
}