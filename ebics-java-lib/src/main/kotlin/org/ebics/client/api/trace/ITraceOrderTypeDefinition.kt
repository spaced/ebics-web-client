package org.ebics.client.api.trace

import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.IEbicsService

interface ITraceOrderTypeDefinition {
    val adminOrderType: EbicsAdminOrderType
    val ebicsServiceType: IEbicsService?
    val businessOrderType: String?
}