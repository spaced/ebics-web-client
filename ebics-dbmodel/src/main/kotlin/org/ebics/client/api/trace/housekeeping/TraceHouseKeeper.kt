package org.ebics.client.api.trace.housekeeping

import org.ebics.client.api.trace.TraceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
open class TraceHouseKeeper(private val traceRepository: TraceRepository): ITraceHouseKeeper {

    //Transactional here should solve error: No EntityManager with actual transaction available for current thread
    @Transactional
    override fun removeAllFilesOlderThan(dateTime: ZonedDateTime): Long {
        return traceRepository.deleteByDateTimeLessThan(dateTime)
    }
}