package org.ebics.client.api.trace

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.exception.EbicsException
import org.ebics.client.exception.IErrorCodeText
import org.ebics.client.interfaces.ContentFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TraceService(
    private val traceRepository: TraceRepository,
    private var traceEnabled: Boolean = true
) : TraceManager {

    override fun trace(contentFactory: ContentFactory, traceSession: IBaseTraceSession, request: Boolean) {
        with(traceSession) {
            val bankConnection = (traceSession as? IBankConnectionTraceSession)?.bankConnection as? BankConnectionEntity
            val bank = bank as? Bank
            val traceEntry = traceRepository.save(
                TraceEntry(
                    null,
                    //The non UTF-8 characters would be marked as � (U+FFFD)
                    contentFactory.getFactoryContent().decodeToString(),
                    contentFactory.getFactoryContent(),
                    bankConnection,
                    bank,
                    sessionId,
                    orderNumber,
                    ebicsVersion,
                    upload,
                    request,
                    orderType = orderType as OrderTypeDefinition,
                    traceType = TraceType.EbicsEnvelope,
                    traceCategory = if (request) TraceCategory.Request else TraceCategory.HttpResponseOk,
                )
            )
            traceSession.lastTraceId = traceEntry.id
        }
    }

    override fun traceException(exception: Exception, traceSession: IBaseTraceSession) {
        with(traceSession) {
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
                    orderType = orderType as OrderTypeDefinition,
                    traceType = TraceType.EbicsEnvelope,
                    traceCategory = TraceCategory.fromException(exception),
                    errorStackTrace = exception.stackTraceToString().take(250),
                    errorMessage = exception.message?.take(250),
                    errorCode = (exception as? IErrorCodeText)?.errorCode,
                    errorCodeText = (exception as? IErrorCodeText)?.errorCodeText,
                )
            )
        }
    }

    override fun updateLastTrace(
        traceSession: IBaseTraceSession,
        traceCategory: TraceCategory,
        exception: EbicsException?
    ) {
        val lastTraceId = traceSession.lastTraceId
        val updatedRows = if (lastTraceId != null) {
            if (exception != null) {
                traceRepository.updateTraceCategoryAndErrorDetailsById(
                    traceCategory, errorCode = (exception as? IErrorCodeText)?.errorCode,
                    errorCodeText = (exception as? IErrorCodeText)?.errorCodeText,
                    errorMessage = exception.message?.take(250),
                    errorStackTrace = exception.stackTraceToString().take(250), lastTraceId
                )
            } else {
                traceRepository.updateTraceCategoryById(traceCategory, lastTraceId)
            }
        } else {
            0
        }
        if (updatedRows == 0)
            logger.warn("There is no previous trace entry to be updated, " +
                    "the function call updateLastTrace expect previous call trace !. " +
                    "The traced category: $traceCategory, exception $exception")
    }

    override fun setTraceEnabled(enabled: Boolean) {
        traceEnabled = enabled
    }

    override fun updateSessionOrderNumber(traceSession: IBankConnectionTraceSession, orderNumber: String) {
        val currentOrderNumber = traceSession.orderNumber
        if (currentOrderNumber == null)
            traceRepository.updateNullOrderNumber(traceSession.sessionId, orderNumber)
        else
            traceRepository.updateNonNullOrderNumber(traceSession.sessionId, orderNumber, currentOrderNumber)
        traceSession.orderNumber = orderNumber
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

    companion object {
        private val logger = LoggerFactory.getLogger(TraceService::class.java)
    }
}
