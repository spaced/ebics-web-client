package org.ebics.client.ebicsrestapi.utils

import org.ebics.client.api.trace.IFileService
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.trace.IBankConnectionTraceSession
import org.ebics.client.model.EbicsSession
import org.ebics.client.model.EbicsVersion
import org.springframework.stereotype.Service

@Service
class FileDownloadCache(private val fileService: IFileService,
                        private val fileDownloadH004: org.ebics.client.filetransfer.h004.FileDownload,
                        private val fileDownloadH005: org.ebics.client.filetransfer.h005.FileDownload,) : IFileDownloadCache {
    override fun getLastFileCached(
        session: EbicsSession,
        traceSession: IBankConnectionTraceSession,
        orderType: OrderTypeDefinition,
        ebicsVersion: EbicsVersion,
        useCache: Boolean,
    ): ByteArray {
        return if (useCache) {
            try {
                //Try to get file from cache
                val traceEntry = fileService.getLastDownloadedFile(
                    orderType,
                    session.user as BankConnectionEntity,
                    ebicsVersion
                )
                    return requireNotNull(traceEntry.textMessageBody?.toByteArray() ?: traceEntry.binaryMessageBody)
                        {"Unexpected failure, the trace entry doen't have content. This should be never the case and it should be prevented by correct JPA query in getLastDownloadedFile"}
            } catch (e: NoSuchElementException) {
                //In case the cache is empty we retrieve file online
                retrieveFileOnlineAndStoreToCache(session, traceSession, orderType, ebicsVersion)
            }
        } else {
            //Cache is not required, so online request is made
            retrieveFileOnlineAndStoreToCache(session, traceSession, orderType, ebicsVersion)
        }
    }

    override fun retrieveFileOnlineAndStoreToCache(
        session: EbicsSession,
        traceSession: IBankConnectionTraceSession,
        orderType: OrderTypeDefinition,
        ebicsVersion: EbicsVersion
    ): ByteArray {
        val outputStream =
            if (ebicsVersion == EbicsVersion.H005) {
                fileDownloadH005.fetchFile(
                    session,
                    traceSession,
                    org.ebics.client.order.h005.EbicsDownloadOrder(
                        orderType.adminOrderType,
                        orderType.ebicsServiceType,
                        null,
                        null
                    )
                )
            } else {
                fileDownloadH004.fetchFile(
                    session,
                    traceSession,
                    org.ebics.client.order.h004.EbicsDownloadOrder(
                        orderType.adminOrderType,
                        orderType.businessOrderType,
                        null,
                        null
                    )
                )
            }
        fileService.addDownloadedFile(
            traceSession,
            outputStream.toByteArray(),
        )
        return outputStream.toByteArray()
    }
}