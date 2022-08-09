package org.ebics.client.api.trace.h005

import org.ebics.client.api.trace.IBankConnectionTraceSession
import org.ebics.client.order.IOrderTypeDefinition30

interface ITraceSession : IBankConnectionTraceSession {
    override val orderType: IOrderTypeDefinition30
}