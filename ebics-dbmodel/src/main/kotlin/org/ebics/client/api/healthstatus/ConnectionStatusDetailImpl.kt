package org.ebics.client.api.healthstatus

import org.ebics.client.exception.EbicsException
import java.time.ZonedDateTime

class ConnectionStatusDetailImpl(
    override val totalCount: Int,
    override val okCount: Int,
    override val errorCount: Int,
    override val healthStatus: HealthStatusType,
    override val errorRate: Int,
    override val okRate: Int,
    override val lastError: EbicsException?,
    override val lastErrorTimestamp: ZonedDateTime?,
    override val lastOkTimestamp: ZonedDateTime?,
) : ConnectionStatusDetail