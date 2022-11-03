package org.ebics.client.api.healthstatus

import org.ebics.client.exception.EbicsException
import java.time.ZonedDateTime

class ConnectionStatusDetail(
    val totalCount: Int,
    val okCount: Int,
    val errorCount: Int,
    val healthStatus: HealthStatusType,
    val errorRate: Int,
    val okRate: Int,
    val lastError: EbicsException?,
    val lastErrorTimestamp: ZonedDateTime?,
    val lastOkTimestamp: ZonedDateTime?,
    val actualStatisticsFrom: ZonedDateTime,
    val lastErrorOkStatisticsFrom: ZonedDateTime,
)