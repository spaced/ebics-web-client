package org.ebics.client.api.trace.housekeeping

import java.time.ZonedDateTime

interface ITraceHouseKeeper {
    fun removeAllFilesOlderThan(dateTime: ZonedDateTime)
}