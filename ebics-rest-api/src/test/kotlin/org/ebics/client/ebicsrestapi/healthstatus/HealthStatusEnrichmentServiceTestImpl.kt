package org.ebics.client.ebicsrestapi.healthstatus

import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.healthstatus.*
import org.ebics.client.exception.EbicsException
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
                EbicsException("Test exception"),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                ZonedDateTime.now().minusHours(1),
                ZonedDateTime.now().minusHours(1)
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
                EbicsException("Test exception"),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                ZonedDateTime.now().minusHours(1),
                ZonedDateTime.now().minusHours(1)
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