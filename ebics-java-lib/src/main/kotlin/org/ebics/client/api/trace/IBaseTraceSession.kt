package org.ebics.client.api.trace

import org.ebics.client.api.EbicsBank
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.IOrderTypeDefinition

interface IBaseTraceSession {
    val bank: EbicsBank
    val orderType: IOrderTypeDefinition
    val upload: Boolean
    val request: Boolean
    val ebicsVersion: EbicsVersion
    val sessionId: String
    val orderNumber: String?
}