package org.ebics.client.order.h005

import org.ebics.client.order.AbstractEbicsDownloadOrder
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.IEbicsService
import java.util.*

/**
 * H005 Download order
 * @param ebicsService the ECBIS service for BTU
 * @param startDate start date (for historical downloads only)
 * @param endDate end date (for historical downloads only)
 */
class EbicsDownloadOrder
(adminOrderType: EbicsAdminOrderType = EbicsAdminOrderType.BTD, val orderService: IEbicsService?, startDate: Date?, endDate: Date?, params: Map<String, String> = emptyMap()) :
    AbstractEbicsDownloadOrder(adminOrderType, startDate, endDate, params) {
    override fun toString(): String {
        return if (orderService == null)
            "AdminOrderType=$adminOrderType"
        else {
            if (startDate == null && endDate == null)
                "BTD=[$orderService]"
            else
                "BTD=[$orderService] start=$startDate end=$endDate"
        }

    }
}