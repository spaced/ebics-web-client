package org.ebics.client.api.healthstatus

import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.trace.TraceCategory
import org.ebics.client.api.trace.TraceEntry
import org.ebics.client.api.trace.TraceRepository
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
    @Value("\${health.statistics.lastErrorOkNotOlderThanMinutest:120}") val lastErrorOkNotOlderThanMinutest: Long,
    @Value("\${health.statistics.actualStatisticsNotOlderThanMinutes:120}") val actualStatisticsNotOlderThanMinutes: Long,
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
        val bankConnectionIds = bankConnections.map { bc -> bc.id!! }.toSet()

        logger.debug("Enriching bank connection ids: {} with statistics started", bankConnectionIds)
        val actualTimeStamp = ZonedDateTime.now()

        val computedActualStatisticsNotOlderThan =
            actualStatisticsNotOlderThan ?: actualTimeStamp.minusMinutes(actualStatisticsNotOlderThanMinutes)

        val lastErrorOkNotOlderThan = actualTimeStamp.minusMinutes(lastErrorOkNotOlderThanMinutest)

        val lastErrorTraces =
            traceRepository.findTopTraceEntriesAndGroupByBankConnection(
                errorCategories,
                lastErrorOkNotOlderThan,
                bankConnectionIds
            )
        val lastOkTraces =
            traceRepository.findTopTraceEntriesAndGroupByBankConnection(
                okCategories,
                lastErrorOkNotOlderThan,
                bankConnectionIds
            )

        val errorCounts = traceRepository.getTraceEntryCountForTraceCategoryInGroupedByBankConnectionId(
            computedActualStatisticsNotOlderThan,
            errorCategories,
            bankConnectionIds
        )
        val okCounts = traceRepository.getTraceEntryCountForTraceCategoryInGroupedByBankConnectionId(
            computedActualStatisticsNotOlderThan,
            okCategories,
            bankConnectionIds
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
                lastErrorTrace,
                computedActualStatisticsNotOlderThan,
                lastErrorOkNotOlderThan,
                actualTimeStamp
            )
        }.also {
            logger.debug("Enriching bank connection ids: {} with statistics finished",bankConnectionIds)
        }
    }

    private fun enrichBankConnectionWithStatus(
        bankConnection: BankConnectionEntity,
        okCount: Int,
        errorCount: Int,
        lastOkTrace: Optional<TraceEntry>,
        lastErrorTrace: Optional<TraceEntry>,
        actualStatisticsNotOlderThan: ZonedDateTime,
        lastErrorOkNotOlderThan: ZonedDateTime,
        actualTimeStamp: ZonedDateTime,
    ): BankConnectionWithHealthStatus {
        val totalCount = errorCount + okCount
        val lastException = lastErrorTrace.map { errorTrace -> ApiError.fromTraceEntry(errorTrace) }.orElse(null)
        val lastErrorTimestamp = lastErrorTrace.map { errorTrace -> errorTrace.dateTime }.orElse(null)
        val lastOkTimestamp = lastOkTrace.map { okTrace -> okTrace.dateTime }.orElse(null)
        val connectionStatusDetail = if (totalCount == 0) {
            ConnectionStatusDetail(
                0,
                0,
                0,
                HealthStatusType.Unknown,
                0,
                0,
                lastException,
                lastErrorTimestamp,
                lastOkTimestamp,
                actualStatisticsNotOlderThan,
                actualTimeStamp,
                lastErrorOkNotOlderThan,
                actualTimeStamp,
            )
        } else {
            val errorRate: Int = (errorCount.toFloat()  / totalCount * 100).toInt()
            val okRate = (okCount.toFloat() / totalCount * 100).toInt()
            val healthStatusType = if (okRate > minimalErrorRate)
                HealthStatusType.Ok
            else if (errorRate > minimalErrorRate)
                HealthStatusType.Error
            else
                HealthStatusType.Warning
            ConnectionStatusDetail(
                totalCount,
                okCount,
                errorCount,
                healthStatusType,
                errorRate,
                okRate,
                lastException,
                lastErrorTimestamp,
                lastOkTimestamp,
                actualStatisticsNotOlderThan,
                actualTimeStamp,
                lastErrorOkNotOlderThan,
                actualTimeStamp
            )
        }
        return BankConnectionWithHealthStatus(bankConnection, ConnectionStatus(connectionStatusDetail))
    }

    override fun enrichBankConnectionWithStatus(
        bankConnection: BankConnectionEntity,
        actualStatisticsNotOlderThan: ZonedDateTime?
    ): BankConnectionWithHealthStatus {
        return enrichBankConnectionsWithStatus(listOf(bankConnection), actualStatisticsNotOlderThan)[0]
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HealthStatusEnrichmentServiceImpl::class.java)
    }
}

