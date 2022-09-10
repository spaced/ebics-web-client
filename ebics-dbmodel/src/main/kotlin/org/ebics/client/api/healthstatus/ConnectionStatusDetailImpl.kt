package org.ebics.client.api.healthstatus

class ConnectionStatusDetailImpl(
    override val totalCount: Int,
    override val okCount: Int,
    override val errorCount: Int,
    override val healthStatus: HealthStatusType,
    override val errorRate: Int,
    override val okRate: Int
) : ConnectionStatusDetail