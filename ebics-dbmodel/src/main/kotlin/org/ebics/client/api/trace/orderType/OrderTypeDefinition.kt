package org.ebics.client.api.trace.orderType

import org.ebics.client.api.trace.ITraceOrderTypeDefinition
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.IOrderTypeDefinition
import org.ebics.client.order.IOrderTypeDefinition25
import org.ebics.client.order.IOrderTypeDefinition30
import javax.persistence.Embeddable
import javax.persistence.Embedded

@Embeddable
data class OrderTypeDefinition(
    //For H002-H005
    override val adminOrderType: EbicsAdminOrderType,

    //For H005 order types
    @Embedded
    override val ebicsServiceType: EbicsService? = null,
    //For H002-H004 order types
    override val businessOrderType: String? = null,
) : ITraceOrderTypeDefinition {
    companion object {
        fun fromOrderType(orderType: IOrderTypeDefinition): OrderTypeDefinition {
            return when {
                (IOrderTypeDefinition25::class.java.isAssignableFrom(orderType.javaClass)) ->
                   OrderTypeDefinition(orderType.adminOrderType, businessOrderType = (orderType as IOrderTypeDefinition25).businessOrderType)
                (IOrderTypeDefinition30::class.java.isAssignableFrom(orderType.javaClass)) -> {
                    val ebicsService = (orderType as IOrderTypeDefinition30).service?.let { service -> EbicsService.fromEbicsService(service)}
                    OrderTypeDefinition(orderType.adminOrderType, ebicsServiceType = ebicsService)
                }
                else ->
                    throw IllegalArgumentException("Can't instantiate OrderTypeDefinition, Unsupported ordertype class: '${orderType.javaClass.name}'")
            }
        }
    }
}
