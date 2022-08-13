package org.ebics.client.api.trace.h005

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.EbicsSession
import org.ebics.client.api.EbicsUser
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.IOrderTypeDefinition30
import java.util.*

data class TraceSession(
    override val bankConnection: EbicsUser,
    override val orderType: IOrderTypeDefinition30,
    override val upload: Boolean = true,
    override val request: Boolean = true,
    override val sessionId: String,
    override val ebicsVersion: EbicsVersion = EbicsVersion.H005,
    override val orderNumber: String = UUID.randomUUID().toString(),
    override val bank: EbicsBank = bankConnection.partner.bank,
    override var lastTraceId: Long? = null,
) : ITraceSession {
    constructor(ebicsSession: EbicsSession, orderType: IOrderTypeDefinition30, upload: Boolean = true, request: Boolean = true):
            this(ebicsSession.user, orderType, upload, request, ebicsSession.sessionId)
}