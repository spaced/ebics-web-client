package org.ebics.client.api.trace

interface TraceStatistic {
    val bankConnectionId: Long
    val traceEntryCount: Int
}