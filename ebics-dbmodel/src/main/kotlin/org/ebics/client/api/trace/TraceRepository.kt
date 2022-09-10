package org.ebics.client.api.trace

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Transactional
interface TraceRepository : JpaRepository<TraceEntry, Long>, JpaSpecificationExecutor<TraceEntry> {
    @Modifying
    fun deleteByDateTimeLessThan(dateTime: ZonedDateTime): Long

    @Modifying
    @Query("update TraceEntry t set t.traceCategory = :traceCategory where t.id = :id")
    fun updateTraceCategoryById(@Param("traceCategory") traceCategory: TraceCategory, @Param("id") id: Long): Int

    @Modifying
    @Query(
        "update TraceEntry t set t.traceCategory = :traceCategory, " +
                "t.errorCode = :errorCode, t.errorCodeText = :errorCodeText, " +
                "t.errorMessage = :errorMessage, t.errorStackTrace = :errorStackTrace where t.id = :id"
    )
    fun updateTraceCategoryAndErrorDetailsById(
        @Param("traceCategory") traceCategory: TraceCategory,
        @Param("errorCode") errorCode: String?,
        @Param("errorCodeText") errorCodeText: String?,
        @Param("errorMessage") errorMessage: String?,
        @Param("errorStackTrace") errorStackTrace: String?,
        @Param("id") id: Long
    ): Int

    @Modifying
    @Query("update TraceEntry t set t.orderNumber = :newOrderNumber where t.orderNumber = :currentOrderNumber and t.sessionId = :sessionId")
    fun updateNonNullOrderNumber(
        @Param("sessionId") sessionId: String,
        @Param("newOrderNumber") newOrderNumber: String,
        @Param("currentOrderNumber") currentOrderNumber: String
    ): Int

    @Modifying
    @Query("update TraceEntry t set t.orderNumber = :newOrderNumber where t.orderNumber is null and t.sessionId = :sessionId")
    fun updateNullOrderNumber(
        @Param("sessionId") sessionId: String,
        @Param("newOrderNumber") newOrderNumber: String
    ): Int

    @Query("select count(t.id) from TraceEntry t where t.bankConnection.id = :bankConnectionId and t.dateTime > :notOlderThan and t.traceCategory in :traceCategoryIn")
    fun getTraceEntryCountByBankConnectionIdAndTraceCategoryIn(
        @Param("bankConnectionId") bankConnectionId: Long,
        @Param("notOlderThan") notOlderThan: LocalDateTime,
        @Param("traceCategoryIn") traceCategoryIn: Set<TraceCategory>
    ): Int

    @Query("select count(t.id) from TraceEntry t t.dateTime > :notOlderThan and t.traceCategory in :traceCategoryIn groupBy t.bankConnection.id")
    fun getTraceEntryCountForTraceCategoryInGroupedByBankConnectionId(
        @Param("notOlderThan") notOlderThan: LocalDateTime,
        @Param("traceCategoryIn") traceCategoryIn: Set<TraceCategory>
    ): Int

    @Query("select t from TraceEntry t t.dateTime > :notOlderThan and t.traceCategory in :traceCategoryIn and t.bankConnection.id = :bankConnectionId")
    fun getTraceEntryByBankConnectionAndCategoryIn(
        @Param("bankConnectionId") bankConnectionId: Long,
        @Param("notOlderThan") notOlderThan: LocalDateTime,
        @Param("traceCategoryIn") traceCategoryIn: Set<TraceCategory>
    ): TraceEntry
}