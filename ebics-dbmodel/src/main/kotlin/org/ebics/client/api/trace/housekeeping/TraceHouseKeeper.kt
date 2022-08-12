package org.ebics.client.api.trace.housekeeping

import org.ebics.client.api.trace.FileService
import org.ebics.client.api.trace.TraceRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
open class TraceHouseKeeper(private val traceRepository: TraceRepository,
                       @Value("\${housekeeping.trace.older-than-days:30}")
                       private val houseKeepOlderThanDays: Long) : ITraceHouseKeeper {

    //Transactional here should solve error: No EntityManager with actual transaction available for current thread
    @Transactional
    override fun removeAllFilesOlderThan(@Value("$\\{value.from.file\\}") dateTime: ZonedDateTime) {
        val numberOfRemovedEntries = traceRepository.deleteByDateTimeLessThan(dateTime)
        logger.info("Total '{}' TraceEntries removed", numberOfRemovedEntries)
    }

    @Scheduled(cron = "0 0 1 * * *")
    fun houseKeeping() {
        logger.info("House keeping of TraceEntries older than {} days", houseKeepOlderThanDays)
        removeAllFilesOlderThan(ZonedDateTime.now().minusDays(houseKeepOlderThanDays))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FileService::class.java)
    }
}