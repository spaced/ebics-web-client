package org.ebics.client.api.healthstatus

import java.time.ZonedDateTime

class ConnectionStatusDetail(
    val totalCount: Int,
    val okCount: Int,
    val errorCount: Int,
    val healthStatus: HealthStatusType,
    val errorRate: Int,
    val okRate: Int,
    val lastError: ApiError?,
    val lastErrorTimestamp: ZonedDateTime?,
    val lastOkTimestamp: ZonedDateTime?,
    val actualStatisticsFrom: ZonedDateTime,
    val actualStatisticsTo: ZonedDateTime,
    val lastErrorOkStatisticsFrom: ZonedDateTime,
    val lastErrorOkStatisticsTo: ZonedDateTime,
)