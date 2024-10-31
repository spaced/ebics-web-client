package org.ebics.client.api.trace

import org.ebics.client.api.EbicsSession
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsVersion

interface IFileService {
    fun getLastDownloadedFile(
        orderType: ITraceOrderTypeDefinition,
        bankConnection: BankConnectionEntity,
        ebicsVersion: EbicsVersion,
        useSharedPartnerData: Boolean = true
    ): TraceEntry

    fun addDownloadedFile(
        traceSession: IBankConnectionTraceSession,
        fileContent: ByteArray,
    ) {
        addDownloadedFile(
            traceSession.bankConnection as BankConnectionEntity,
            traceSession.orderType, fileContent, traceSession.sessionId, traceSession.orderNumber, traceSession.ebicsVersion
        )
    }

    fun addDownloadedFile(
        bankConnection: BankConnectionEntity,
        orderType: ITraceOrderTypeDefinition,
        fileContent: ByteArray,
        sessionId: String,
        orderNumber: String?,
        ebicsVersion: EbicsVersion,
    ) = addFile(bankConnection, orderType, fileContent, sessionId, orderNumber, ebicsVersion, upload = false, request = false)

    fun addUploadedFile(
        traceSession: IBankConnectionTraceSession,
        fileContent: ByteArray,
    ) {
        addFile(
            traceSession.bankConnection as BankConnectionEntity,
            traceSession.orderType,
            fileContent,
            traceSession.sessionId,
            traceSession.orderNumber,
            traceSession.ebicsVersion,
            upload = true,
            request = true
        )
    }

    fun addUploadedFile(
        session: EbicsSession,
        orderType: ITraceOrderTypeDefinition,
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
        bankConnection: BankConnectionEntity,
        orderType: ITraceOrderTypeDefinition,
        fileContent: ByteArray,
        sessionId: String,
        orderNumber: String?,
        ebicsVersion: EbicsVersion,
        upload: Boolean,
        request: Boolean,
    )
}