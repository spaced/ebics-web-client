package org.ebics.client.api.trace

import org.ebics.client.api.EbicsSession
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsVersion

interface IFileService {
    fun getLastDownloadedFile(
        orderType: OrderTypeDefinition,
        user: BankConnectionEntity,
        ebicsVersion: EbicsVersion,
        useSharedPartnerData: Boolean = true
    ): TraceEntry

    fun addDownloadedFile(
        user: BankConnectionEntity,
        orderType: OrderTypeDefinition,
        fileContent: ByteArray,
        sessionId: String,
        ebicsVersion: EbicsVersion,
    ) = addFile(user, orderType, fileContent, sessionId, null, ebicsVersion, upload = false, request = false)

    fun addUploadedFile(
        session: EbicsSession,
        orderType: OrderTypeDefinition,
        fileContent: ByteArray,
        orderNumber: String,
        ebicsVersion: EbicsVersion,
    ) = addFile(
        session.user as BankConnectionEntity,
        orderType,
        fileContent,
        session.sessionId,
        orderNumber,
        ebicsVersion,
        upload = true,
        request = true
    )

    fun addFile(
        user: BankConnectionEntity,
        orderType: OrderTypeDefinition,
        fileContent: ByteArray,
        sessionId: String,
        orderNumber: String?,
        ebicsVersion: EbicsVersion,
        upload: Boolean,
        request: Boolean,
    )
}