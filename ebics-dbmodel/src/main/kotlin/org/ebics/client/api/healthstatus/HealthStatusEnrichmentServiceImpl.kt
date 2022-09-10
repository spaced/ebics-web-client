package org.ebics.client.api.healthstatus

import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.trace.TraceCategory
import org.ebics.client.api.trace.TraceRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HealthStatusEnrichmentServiceImpl(
    private val traceRepository: TraceRepository,
    @Value("health.statistics.minimalOkRate:90") val minimalOkRate: Int,
    @Value("health.statistics.minimalErrorRate:50") val minimalErrorRate: Int,
    @Value("health.statistics.lastOkErrorNotOlderThanDays:5") val lastErrorOkNotOlderThanDays: Long,
    @Value("health.statistics.actualStatisticsOlderThanMinutes:5") val actualStatisticsNotOlderThanMinutes: Long,
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
        notOlderThan: LocalDateTime
    ): List<BankConnectionWithHealthStatus> {
        TODO("not impl yet")
    }

    override fun enrichBankConnectionWithStatus(
        bankConnection: BankConnectionEntity,
        actualStatisticsNotOlderThan: LocalDateTime?
    ): BankConnectionWithHealthStatus {
        val actualStatisticsNotOlderThan =
            actualStatisticsNotOlderThan ?: LocalDateTime.now().minusMinutes(actualStatisticsNotOlderThanMinutes)

        val bankConnectionId =
            requireNotNull(bankConnection.id) { "enrichBankConnectionWithStatus expect saved bankConnection with non null id" }
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
        val statisticsDetail = if (totalCount == 0) {
            ConnectionStatusDetailImpl(0,0,0,HealthStatusType.Unknown, 0, 0)
        } else {
            val errorRate = errorCount / totalCount
            val okRate = okCount / totalCount
            val healthStatusType = if (okRate > minimalErrorRate)
                HealthStatusType.Ok
            else if (errorRate > minimalErrorRate)
                HealthStatusType.Error
            else
                HealthStatusType.Warning
            ConnectionStatusDetailImpl(totalCount, okCount, errorCount, healthStatusType, errorRate, okRate)
        }

        if (okCount == 0 || errorCount == 0) {
            val lastErrorOkNotOlderThan = LocalDateTime.now().minusDays(lastErrorOkNotOlderThanDays)
            if (okCount == 0) {
                val lastOkTrace = traceRepository.getTraceEntryByBankConnectionAndCategoryIn(bankConnectionId, lastErrorOkNotOlderThan, okCategories)
            }

            if (errorCount == 0) {
                val lastErrorTrace = traceRepository.getTraceEntryByBankConnectionAndCategoryIn(bankConnectionId, lastErrorOkNotOlderThan, errorCategories)
            }
        }
        TODO("Get ")
        //val connectionStatus = ConnectionStatus(statisticsDetail, null)
        //return BankConnectionWithHealthStatus(bankConnection, connectionStatus)
    }
}

