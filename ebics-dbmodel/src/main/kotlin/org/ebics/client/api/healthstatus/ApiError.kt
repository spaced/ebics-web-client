package org.ebics.client.api.healthstatus

import org.ebics.client.api.trace.TraceEntry
import java.time.ZonedDateTime

data class ApiError (
    val timestamp: ZonedDateTime,
    val message: String
) {
    companion object {
        fun fromTraceEntry(traceEntry: TraceEntry): ApiError? {
            return if (traceEntry.errorMessage != null)
                ApiError(traceEntry.dateTime, traceEntry.errorMessage)
            else
                null
        }
    }
}