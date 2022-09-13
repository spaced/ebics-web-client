package org.ebics.client.api.healthstatus

import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.trace.TraceCategory
import org.ebics.client.api.trace.TraceRepository
import org.ebics.client.exception.EbicsException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class HealthStatusEnrichmentServiceImpl(
    private val traceRepository: TraceRepository,
    @Value("\${health.statistics.minimalOkRate:90}") val minimalOkRate: Int,
    @Value("\${health.statistics.minimalErrorRate:50}") val minimalErrorRate: Int,
    @Value("\${health.statistics.lastOkErrorNotOlderThanDays:5}") val lastErrorOkNotOlderThanDays: Long,
    @Value("\${health.statistics.actualStatisticsOlderThanMinutes:5}") val actualStatisticsNotOlderThanMinutes: Long,
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
        notOlderThan: ZonedDateTime?
    ): List<BankConnectionWithHealthStatus> {
        TODO("not impl yet")
    }

    override fun enrichBankConnectionWithStatus(
        bankConnection: BankConnectionEntity,
        actualStatisticsNotOlderThan: ZonedDateTime?
    ): BankConnectionWithHealthStatus {
        val actualStatisticsNotOlderThan =
            actualStatisticsNotOlderThan ?: ZonedDateTime.now().minusMinutes(actualStatisticsNotOlderThanMinutes)

        val bankConnectionId =
            requireNotNull(bankConnection.id) { "enrichBankConnectionWithStatus expect saved bankConnection with non null id" }
        val lastErrorOkNotOlderThan = ZonedDateTime.now().minusDays(lastErrorOkNotOlderThanDays)

        val lastOkTimestamp =
            traceRepository.getTraceEntryByBankConnectionAndCategoryIn(
                bankConnectionId,
                lastErrorOkNotOlderThan,
                okCategories
            ).map { traceEntry -> traceEntry.dateTime }.orElse(null)
        
        val lastErrorTrace = traceRepository.getTraceEntryByBankConnectionAndCategoryIn(
            bankConnectionId,
            lastErrorOkNotOlderThan,
            errorCategories
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
            ConnectionStatusDetailImpl(0, 0, 0, HealthStatusType.Unknown, 0, 0, lastException, lastErrorTimestamp, lastOkTimestamp)
        } else {
            val errorRate = errorCount / totalCount
            val okRate = okCount / totalCount
            val healthStatusType = if (okRate > minimalErrorRate)
                HealthStatusType.Ok
            else if (errorRate > minimalErrorRate)
                HealthStatusType.Error
            else
                HealthStatusType.Warning
            ConnectionStatusDetailImpl(totalCount, okCount, errorCount, healthStatusType, errorRate, okRate, lastException, lastErrorTimestamp, lastOkTimestamp)
        }

        return BankConnectionWithHealthStatus(bankConnection, ConnectionStatusImpl(connectionStatusDetail))
    }
}

