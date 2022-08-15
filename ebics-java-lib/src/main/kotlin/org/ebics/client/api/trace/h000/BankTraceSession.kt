package org.ebics.client.api.trace.h000

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.trace.ITraceOrderTypeDefinition
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.IEbicsService
import org.ebics.client.order.IOrderTypeDefinition25
import java.util.*

data class BankTraceSession(
    override val bank: EbicsBank,
    override val upload: Boolean,
    override val request: Boolean,
    override val sessionId: String,
    override val ebicsVersion: EbicsVersion,
    override val orderType: ITraceOrderTypeDefinition,
    override val orderNumber: String? = UUID.randomUUID().toString(),
    override var lastTraceId: Long? = null
) : IBankTraceSession {
    constructor(bank: EbicsBank, upload: Boolean, request: Boolean, sessionId: String, ebicsVersion: EbicsVersion, orderType: IOrderTypeDefinition25)
    : this(bank, upload, request, sessionId, ebicsVersion, object : ITraceOrderTypeDefinition {
        override val adminOrderType: EbicsAdminOrderType
            get() = orderType.adminOrderType
        override val ebicsServiceType: IEbicsService?
            get() = null
        override val businessOrderType: String?
            get() = orderType.businessOrderType
    })
}