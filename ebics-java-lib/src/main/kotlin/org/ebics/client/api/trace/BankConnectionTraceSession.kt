package org.ebics.client.api.trace

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.EbicsSession
import org.ebics.client.api.EbicsUser
import org.ebics.client.model.EbicsVersion
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
}