package org.ebics.client.api.trace.h004

import org.ebics.client.api.trace.IBankConnectionTraceSession
import org.ebics.client.order.IOrderTypeDefinition25

interface ITraceSession : IBankConnectionTraceSession {
    override val orderType: IOrderTypeDefinition25
}