package org.ebics.client.api.trace

import org.ebics.client.api.EbicsBank
import org.ebics.client.model.EbicsVersion

interface IBaseTraceSession {
    val bank: EbicsBank
    val orderType: ITraceOrderTypeDefinition
    val upload: Boolean
    val request: Boolean
    val ebicsVersion: EbicsVersion
    val sessionId: String
    var orderNumber: String?

    /**
     * Here is stored last trace id
     * This is used for eventual update of the last trace entry in this session
     */
    var lastTraceId: Long?
}