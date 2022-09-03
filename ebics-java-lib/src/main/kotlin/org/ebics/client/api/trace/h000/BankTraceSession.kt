package org.ebics.client.api.trace.h000

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.trace.ITraceOrderTypeDefinition
import org.ebics.client.model.EbicsVersion
import java.util.*

data class BankTraceSession(
    override val bank: EbicsBank,
    override val upload: Boolean,
    override val request: Boolean,
    override val sessionId: String,
    override val ebicsVersion: EbicsVersion,
    override val orderType: ITraceOrderTypeDefinition,
    override var orderNumber: String? = UUID.randomUUID().toString(),
    override var lastTraceId: Long? = null
) : IBankTraceSession