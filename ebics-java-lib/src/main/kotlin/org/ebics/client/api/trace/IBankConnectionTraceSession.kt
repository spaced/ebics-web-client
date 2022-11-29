package org.ebics.client.api.trace

import org.ebics.client.api.EbicsUser

interface IBankConnectionTraceSession : IBaseTraceSession {
    val bankConnection: EbicsUser
}

