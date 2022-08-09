package org.ebics.client.api.trace.h000

import org.ebics.client.api.EbicsBank
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.IOrderTypeDefinition25
import java.util.*

data class BankTraceSession(
    override val bank: EbicsBank,
    override val upload: Boolean,
    override val request: Boolean,
    override val sessionId: String,
    override val ebicsVersion: EbicsVersion,
    override val orderType: IOrderTypeDefinition25,
    override val orderNumber: String? = UUID.randomUUID().toString()
) : IBankTraceSession