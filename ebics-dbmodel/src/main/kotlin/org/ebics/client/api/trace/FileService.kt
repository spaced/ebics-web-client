package org.ebics.client.api.trace

import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsVersion
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
open class FileService(private val traceRepository: TraceRepository) : IFileService {

    override fun getLastDownloadedFile(
        orderType: ITraceOrderTypeDefinition,
        bankConnection: BankConnectionEntity,
        ebicsVersion: EbicsVersion,
        useSharedPartnerData: Boolean
    ): TraceEntry {
        val authCtx = AuthenticationContext.fromSecurityContext()
        return traceRepository
            .findAll(
                fileDownloadFilter(authCtx.name, orderType, bankConnection, ebicsVersion, useSharedPartnerData),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "dateTime"))
            )
            .single { it.hasReadAccess(authCtx) }.also {
                logger.debug("File retrieved from file service, orderType {}, bank connection id={}, userId={}, ebicsVersion={}", orderType.toString(), bankConnection.id, bankConnection.userId, ebicsVersion)
            }
    }

    override fun addFile(
        bankConnection: BankConnectionEntity,
        orderType: ITraceOrderTypeDefinition,
        fileContent: ByteArray,
        sessionId: String,
        orderNumber: String?,
        ebicsVersion: EbicsVersion,
        upload: Boolean,
        request: Boolean,
    ) {
        traceRepository.save(
            TraceEntry(
                null,
                fileContent.decodeToString(),
                fileContent,
                bankConnection, bankConnection.partner.bank,
                sessionId,
                orderNumber,
                ebicsVersion,
                upload,
                request,
                orderType = OrderTypeDefinition.fromOrderTypeDefinition(orderType),
                traceType = TraceType.Content,
                traceCategory = null
            )
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FileService::class.java)
    }
}