package org.ebics.client.api.healthstatus

import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.trace.TraceCategory
import org.ebics.client.api.trace.TraceEntry
import org.ebics.client.api.trace.TraceRepository
import org.ebics.client.exception.EbicsException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class HealthStatusEnrichmentServiceImpl(
    private val traceRepository: TraceRepository,
    @Value("\${health.statistics.minimalOkRate:90}") val minimalOkRate: Int,
    @Value("\${health.statistics.minimalErrorRate:50}") val minimalErrorRate: Int,
    @Value("\${health.statistics.lastOkErrorNotOlderThanMinutes:120}") val lastErrorOkNotOlderThanMinutest: Long,
    @Value("\${health.statistics.actualStatisticsOlderThanMinutes:120}") val actualStatisticsNotOlderThanMinutes: Long,
) : HealthStatusEnrichmentService {
    //We consider all kind of errors which can happen during the whole EBICS request/response flow
    private val errorCategories = setOf(
        TraceCategory.RequestError,
        TraceCategory.EbicsResponseError,
        TraceCategory.HttpResponseError,
        TraceCategory.GeneralError
    )

    //We consider only EBICS ok responses (everything else is not yet final)
    private val okCategories = setOf(TraceCategory.EbicsResponseOk)

    override fun enrichBankConnectionsWithStatus(
        bankConnections: List<BankConnectionEntity>,
        actualStatisticsNotOlderThan: ZonedDateTime?
    ): List<BankConnectionWithHealthStatus> {
        logger.debug("Enriching bank connection with statistics started")
        val actualStatisticsNotOlderThan =
            actualStatisticsNotOlderThan ?: ZonedDateTime.now().minusMinutes(actualStatisticsNotOlderThanMinutes)

        val lastErrorOkNotOlderThan = ZonedDateTime.now().minusMinutes(lastErrorOkNotOlderThanMinutest)

        val lastErrorTraces =
            traceRepository.findTopTraceEntriesAndGroupByBankConnection(errorCategories, lastErrorOkNotOlderThan)
        val lastOkTraces =
            traceRepository.findTopTraceEntriesAndGroupByBankConnection(okCategories, lastErrorOkNotOlderThan)

        val errorCounts = traceRepository.getTraceEntryCountForTraceCategoryInGroupedByBankConnectionId(
            actualStatisticsNotOlderThan,
            errorCategories
        )
        val okCounts = traceRepository.getTraceEntryCountForTraceCategoryInGroupedByBankConnectionId(
            actualStatisticsNotOlderThan,
            okCategories
        )

        return bankConnections.map { bankConnection ->
            val errorCount = errorCounts.find { errorCount -> errorCount.bankConnectionId == bankConnection.id }
            val okCount = okCounts.find { okCount -> okCount.bankConnectionId == bankConnection.id }
            val lastErrorTrace =
                Optional.ofNullable(lastErrorTraces.find { traceEntry -> traceEntry.bankConnection?.id == bankConnection.id })
            val lastOkTrace =
                Optional.ofNullable(lastOkTraces.find { traceEntry -> traceEntry.bankConnection?.id == bankConnection.id })
            enrichBankConnectionWithStatus(
                bankConnection,
                okCount?.traceEntryCount ?: 0,
                errorCount?.traceEntryCount ?: 0,
                lastOkTrace,
                lastErrorTrace
            )
        }.also {
            logger.debug("Enriching bank connection with statistics finished")
        }
    }

    private fun enrichBankConnectionWithStatus(
        bankConnection: BankConnectionEntity,
        okCount: Int,
        errorCount: Int,
        lastOkTrace: Optional<TraceEntry>,
        lastErrorTrace: Optional<TraceEntry>
    ): BankConnectionWithHealthStatus {
        val totalCount = errorCount + okCount
        val lastException = lastErrorTrace.map { errorTrace -> EbicsException(errorTrace.errorMessage) }.orElse(null)
        val lastErrorTimestamp = lastErrorTrace.map { errorTrace -> errorTrace.dateTime }.orElse(null)
        val lastOkTimestamp = lastErrorTrace.map { okTrace -> okTrace.dateTime }.orElse(null)
        val connectionStatusDetail = if (totalCount == 0) {
            ConnectionStatusDetailImpl(
                0,
                0,
                0,
                HealthStatusType.Unknown,
                0,
                0,
                lastException,
                lastErrorTimestamp,
                lastOkTimestamp
            )
        } else {
            val errorRate = errorCount / totalCount
            val okRate = okCount / totalCount
            val healthStatusType = if (okRate > minimalErrorRate)
                HealthStatusType.Ok
            else if (errorRate > minimalErrorRate)
                HealthStatusType.Error
            else
                HealthStatusType.Warning
            ConnectionStatusDetailImpl(
                totalCount,
                okCount,
                errorCount,
                healthStatusType,
                errorRate,
                okRate,
                lastException,
                lastErrorTimestamp,
                lastOkTimestamp
            )
        }
        return BankConnectionWithHealthStatus(bankConnection, ConnectionStatusImpl(connectionStatusDetail))
    }

    override fun enrichBankConnectionWithStatus(
        bankConnection: BankConnectionEntity,
        actualStatisticsNotOlderThan: ZonedDateTime?
    ): BankConnectionWithHealthStatus {
        val actualStatisticsNotOlderThan =
            actualStatisticsNotOlderThan ?: ZonedDateTime.now().minusMinutes(actualStatisticsNotOlderThanMinutes)

        val bankConnectionId =
            requireNotNull(bankConnection.id) { "enrichBankConnectionWithStatus expect saved bankConnection with non null id" }
        val lastErrorOkNotOlderThan = ZonedDateTime.now().minusMinutes(lastErrorOkNotOlderThanMinutest)

        val lastOkTimestamp =
            traceRepository.findTopTraceEntryByBankConnectionIdAndTraceCategoryInAndDateTimeGreaterThanOrderByDateTimeDesc(
                bankConnectionId,
                okCategories,
                lastErrorOkNotOlderThan
            ).map { traceEntry -> traceEntry.dateTime }.orElse(null)

        val lastErrorTrace =
            traceRepository.findTopTraceEntryByBankConnectionIdAndTraceCategoryInAndDateTimeGreaterThanOrderByDateTimeDesc(
                bankConnectionId,
                errorCategories,
                lastErrorOkNotOlderThan
            )
        val lastException = lastErrorTrace.map { errorTrace -> EbicsException(errorTrace.errorMessage) }.orElse(null)
        val lastErrorTimestamp = lastErrorTrace.map { errorTrace -> errorTrace.dateTime }.orElse(null)

        val errorCount = traceRepository.getTraceEntryCountByBankConnectionIdAndTraceCategoryIn(
            bankConnectionId,
            actualStatisticsNotOlderThan,
            errorCategories,
        )
        val okCount = traceRepository.getTraceEntryCountByBankConnectionIdAndTraceCategoryIn(
            bankConnectionId,
            actualStatisticsNotOlderThan,
            okCategories,
        )
        val totalCount = errorCount + okCount
        val connectionStatusDetail = if (totalCount == 0) {
            ConnectionStatusDetailImpl(
                0,
                0,
                0,
                HealthStatusType.Unknown,
                0,
                0,
                lastException,
                lastErrorTimestamp,
                lastOkTimestamp
            )
        } else {
            val errorRate = errorCount / totalCount
            val okRate = okCount / totalCount
            val healthStatusType = if (okRate > minimalErrorRate)
                HealthStatusType.Ok
            else if (errorRate > minimalErrorRate)
                HealthStatusType.Error
            else
                HealthStatusType.Warning
            ConnectionStatusDetailImpl(
                totalCount,
                okCount,
                errorCount,
                healthStatusType,
                errorRate,
                okRate,
                lastException,
                lastErrorTimestamp,
                lastOkTimestamp
            )
        }

        return BankConnectionWithHealthStatus(bankConnection, ConnectionStatusImpl(connectionStatusDetail))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HealthStatusEnrichmentServiceImpl::class.java)
    }
}

