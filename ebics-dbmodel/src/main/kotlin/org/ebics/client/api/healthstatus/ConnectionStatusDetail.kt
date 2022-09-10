package org.ebics.client.api.healthstatus

interface ConnectionStatusDetail {
    val totalCount: Int
    val okCount: Int
    val errorCount: Int
    val healthStatus: HealthStatusType
    val errorRate: Int
    val okRate: Int
}