package org.ebics.client.api.trace

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.EbicsSession
import org.ebics.client.api.EbicsUser
import org.ebics.client.api.trace.IBankConnectionTraceSession
import org.ebics.client.api.trace.ITraceOrderTypeDefinition
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.IEbicsService
import org.ebics.client.order.IOrderTypeDefinition25
import org.ebics.client.order.IOrderTypeDefinition30
import java.util.*

data class BankConnectionTraceSession(
    override val bankConnection: EbicsUser,
    override val orderType: ITraceOrderTypeDefinition,
    override val upload: Boolean = true,
    override val request: Boolean = true,
    override val sessionId: String,
    override val ebicsVersion: EbicsVersion = EbicsVersion.H005,
    override var orderNumber: String? = UUID.randomUUID().toString(),
    override val bank: EbicsBank = bankConnection.partner.bank,
    override var lastTraceId: Long? = null,
) : IBankConnectionTraceSession {
    constructor(ebicsSession: EbicsSession, orderType: ITraceOrderTypeDefinition, upload: Boolean = true, request: Boolean = true):
            this(ebicsSession.user, orderType, upload, request, ebicsSession.sessionId)

    constructor(ebicsSession: EbicsSession, orderType: IOrderTypeDefinition30, upload: Boolean = true, request: Boolean = true):
            this(ebicsSession, object : ITraceOrderTypeDefinition {
                override val adminOrderType: EbicsAdminOrderType
                    get() = orderType.adminOrderType
                override val ebicsServiceType: IEbicsService?
                    get() = orderType.service
                override val businessOrderType: String?
                    get() = null
            } , upload, request)

    constructor(bankConnection: EbicsUser, orderType: IOrderTypeDefinition30, upload: Boolean = true, request: Boolean = true, sessionId: String):
            this(bankConnection, object : ITraceOrderTypeDefinition {
                override val adminOrderType: EbicsAdminOrderType
                    get() = orderType.adminOrderType
                override val ebicsServiceType: IEbicsService?
                    get() = orderType.service
                override val businessOrderType: String?
                    get() = null
            } , upload, request, sessionId)

    constructor(ebicsSession: EbicsSession, orderType: IOrderTypeDefinition25, upload: Boolean = true, request: Boolean = true):
            this(ebicsSession, object : ITraceOrderTypeDefinition {
                override val adminOrderType: EbicsAdminOrderType
                    get() = orderType.adminOrderType
                override val ebicsServiceType: IEbicsService?
                    get() = null
                override val businessOrderType: String?
                    get() = orderType.businessOrderType
            } , upload, request)

    constructor(bankConnection: EbicsUser, orderType: IOrderTypeDefinition25, upload: Boolean = true, request: Boolean = true, sessionId: String):
            this(bankConnection, object : ITraceOrderTypeDefinition {
                override val adminOrderType: EbicsAdminOrderType
                    get() = orderType.adminOrderType
                override val ebicsServiceType: IEbicsService?
                    get() = null
                override val businessOrderType: String?
                    get() = orderType.businessOrderType
            } , upload, request, sessionId)
}