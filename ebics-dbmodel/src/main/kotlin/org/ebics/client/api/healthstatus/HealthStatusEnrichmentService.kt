package org.ebics.client.api.healthstatus

import org.ebics.client.api.bankconnection.BankConnectionEntity
import java.time.LocalDateTime

interface HealthStatusEnrichmentService {
    fun enrichBankConnectionsWithStatus(bankConnections: List<BankConnectionEntity>, useTracesNewerThan: LocalDateTime): List<BankConnectionWithHealthStatus>
    fun enrichBankConnectionWithStatus(bankConnection: BankConnectionEntity, actualStatisticsNotOlderThan: LocalDateTime?): BankConnectionWithHealthStatus
}