package org.ebics.client.api.trace

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.exception.IErrorCodeText
import org.ebics.client.interfaces.ContentFactory
import org.springframework.stereotype.Service
import java.lang.Exception

@Service
class TraceService(
    private val traceRepository: TraceRepository,
    private var traceEnabled: Boolean = true
) : TraceManager {

    override fun trace(content: ContentFactory, traceSession: IBaseTraceSession, request: Boolean) {
        with(traceSession) {
            val orderTypeDefinition = OrderTypeDefinition.fromOrderType(orderType)
            val bankConnection = (traceSession as? IBankConnectionTraceSession)?.bankConnection as? BankConnectionEntity
            val bank = bank as? Bank
            traceRepository.save(
                TraceEntry(
                    null,
                    //The non UTF-8 characters would be marked as � (U+FFFD)
                    content.getFactoryContent().decodeToString(),
                    content.getFactoryContent(),
                    bankConnection,
                    bank,
                    sessionId,
                    orderNumber,
                    ebicsVersion,
                    upload,
                    request,
                    orderType = orderTypeDefinition,
                    traceType = TraceType.EbicsEnvelope,
                    traceCategory = TraceCategory.httpOk,
                )
            )
        }
    }

    override fun traceException(exception: Exception, traceSession: IBaseTraceSession) {
        with(traceSession) {
            val orderTypeDefinition = OrderTypeDefinition.fromOrderType(orderType)
            val bankConnection = (traceSession as? IBankConnectionTraceSession)?.bankConnection as? BankConnectionEntity
            val bank = bank as? Bank
            traceRepository.save(
                TraceEntry(
                    null,
                    null,
                    null,
                    bankConnection,
                    bank,
                    sessionId,
                    orderNumber,
                    ebicsVersion,
                    upload,
                    request = false,
                    orderType = orderTypeDefinition,
                    traceType = TraceType.EbicsEnvelope,
                    traceCategory = TraceCategory.fromException(exception),
                    errorStackTrace = exception.stackTraceToString(),
                    errorMessage = exception.message,
                    errorCode = (exception as? IErrorCodeText)?.errorCode,
                    errorCodeText = (exception as? IErrorCodeText)?.errorCodeText,
                )
            )
        }
    }

    override fun setTraceEnabled(enabled: Boolean) {
        traceEnabled = enabled
    }

    /**
     * Return all traces which are available, and those which are not owned are anonimized
     */
    fun findAllTraces(): List<BaseTraceEntry> {
        return traceRepository.findAll().map { traceEntry ->
            if (traceEntry.hasReadAccess(AuthenticationContext.fromSecurityContext()))
                traceEntry
            else
                AnonymousTraceEntry.anonymizeTraceEntry(traceEntry)
        }
    }

    /**
     * Returns only the own created traces with all access
     */
    fun findOwnTraces(): List<TraceEntry> {
        return traceRepository.findAll().filter { it.hasReadAccess(AuthenticationContext.fromSecurityContext()) }
    }
}