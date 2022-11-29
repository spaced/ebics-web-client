package org.ebics.client.ebicsrestapi.healthstatus

import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.healthstatus.*
import java.time.ZonedDateTime

class HealthStatusEnrichmentServiceTestImpl : HealthStatusEnrichmentService {
    override fun enrichBankConnectionWithStatus(
        bankConnection: BankConnectionEntity,
        actualStatisticsNotOlderThan: ZonedDateTime?
    ): BankConnectionWithHealthStatus {
        val connectionStatus = ConnectionStatus(
            ConnectionStatusDetail(
                10,
                5,
                5,
                HealthStatusType.Warning,
                50,
                50,
                ApiError(ZonedDateTime.now(), "Test exception"),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                ZonedDateTime.now().minusHours(1),
                ZonedDateTime.now(),
                ZonedDateTime.now().minusHours(1),
                ZonedDateTime.now(),
            )
        )
        return BankConnectionWithHealthStatus(bankConnection, connectionStatus)
    }

    override fun enrichBankConnectionsWithStatus(
        bankConnections: List<BankConnectionEntity>,
        actualStatisticsNotOlderThan: ZonedDateTime?
    ): List<BankConnectionWithHealthStatus> {
        val connectionStatus = ConnectionStatus(
            ConnectionStatusDetail(
                10,
                5,
                5,
                HealthStatusType.Warning,
                50,
                50,
                ApiError(ZonedDateTime.now(), "Test exception"),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                ZonedDateTime.now().minusHours(1),
                ZonedDateTime.now(),
                ZonedDateTime.now().minusHours(1),
                ZonedDateTime.now(),
            )
        )
        return bankConnections.map { bankConnection ->
            BankConnectionWithHealthStatus(
                bankConnection,
                connectionStatus
            )
        }
    }
}