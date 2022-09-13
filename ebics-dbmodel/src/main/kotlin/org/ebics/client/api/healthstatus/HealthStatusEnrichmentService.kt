package org.ebics.client.api.healthstatus

import org.ebics.client.api.bankconnection.BankConnectionEntity
import java.time.ZonedDateTime

interface HealthStatusEnrichmentService {
    fun enrichBankConnectionsWithStatus(bankConnections: List<BankConnectionEntity>, actualStatisticsNotOlderThan: ZonedDateTime?): List<BankConnectionWithHealthStatus>
    fun enrichBankConnectionWithStatus(bankConnection: BankConnectionEntity, actualStatisticsNotOlderThan: ZonedDateTime?): BankConnectionWithHealthStatus
}