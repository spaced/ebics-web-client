package org.ebics.client.api.trace

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.model.EbicsVersion
import java.time.ZonedDateTime

class AnonymousTraceEntry(
    override val id: Long?,
    override val bankConnection: BankConnectionEntity?,
    override val bank: Bank?,
    override val ebicsVersion: EbicsVersion,
    override val upload: Boolean,
    override val request: Boolean,
    override val creator: String,
    override val dateTime: ZonedDateTime,
    override val orderType: OrderTypeDefinition?,
    override val traceType: TraceType,
    override val traceCategory: TraceCategory,
    override val errorCode: String?,
    override val errorCodeText: String?,
    override val errorMessage: String?,
    override val errorStackTrace: String?
) : BaseTraceEntry {
    companion object {
        fun anonymizeTraceEntry(traceEntry: TraceEntry): AnonymousTraceEntry {
            with(traceEntry) {
                return AnonymousTraceEntry(
                    id,
                    bankConnection,
                    bank,
                    ebicsVersion,
                    upload,
                    request,
                    creator = "Anonymize",
                    dateTime,
                    orderType,
                    traceType,
                    traceCategory,
                    errorCode,
                    errorCodeText,
                    errorMessage,
                    errorStackTrace
                )
            }
        }
    }
}