package org.ebics.client.api.trace.housekeeping

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
open class TraceHouseKeeperScheduler(private val traceHouseKeeper: TraceHouseKeeper,
                            @Value("\${housekeeping.trace.older-than-days}")
                       private val houseKeepOlderThanDays: Long) {

    init {
        logger.info("scheduler configured '{}'", houseKeepOlderThanDays)
    }

    @Scheduled(cron = "\${housekeeping.trace.cron-expression}")
    fun houseKeeping() {
        logger.info("House keeping of TraceEntries older than {} days", houseKeepOlderThanDays)
        val numberOfRemovedEntries = traceHouseKeeper.removeAllFilesOlderThan(ZonedDateTime.now().minusDays(houseKeepOlderThanDays))
        logger.info("Total '{}' TraceEntries removed", numberOfRemovedEntries)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TraceHouseKeeperScheduler::class.java)
    }
}