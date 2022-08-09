package org.ebics.client.api.trace.h004

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.EbicsSession
import org.ebics.client.api.EbicsUser
import org.ebics.client.interfaces.EbicsRootElement
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.IOrderTypeDefinition25
import java.util.*

data class TraceSession(
    override val bankConnection: EbicsUser,
    override val orderType: IOrderTypeDefinition25,
    override val upload: Boolean = true,
    override val request: Boolean = true,
    override val sessionId: String,
    override val ebicsVersion: EbicsVersion = EbicsVersion.H004,
    override val orderNumber: String = UUID.randomUUID().toString(),
    override val bank: EbicsBank = bankConnection.partner.bank
) : ITraceSession {
    constructor(ebicsSession: EbicsSession, orderType: IOrderTypeDefinition25, upload: Boolean = true, request: Boolean = true):
            this(ebicsSession.user, orderType, upload, request, ebicsSession.sessionId)
}