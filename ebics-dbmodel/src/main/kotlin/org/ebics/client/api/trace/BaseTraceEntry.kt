package org.ebics.client.api.trace

import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.model.EbicsVersion
import java.time.ZonedDateTime

interface BaseTraceEntry {
    val id:Long?

    /**
     * Reference to bank connection if given
     */
    val bankConnection: BankConnectionEntity?

    /**
     * Reference to bank if given
     */
    val bank: Bank?

    /**
     * The EBICS version used for this entry
     */
    val ebicsVersion: EbicsVersion

    /**
     * Is this EBICS upload or download
     */
    val upload: Boolean

    /**
     * Is this request operation or response operation
     */
    val request: Boolean

    /**
     * Web user who created this entry
     */
    val creator: String

    /**
     * Time when was the entry created
     */
    val dateTime: ZonedDateTime

    /**
     * Optional Order Type of this entry
     *  (currently null by standard EBICS tracing)
     */
    val orderType: OrderTypeDefinition?

    val traceType: TraceType

    val traceCategory: TraceCategory?

    //Error relevant fields
    val errorCode: String?
    val errorCodeText: String?
    val errorMessage: String?
    val errorStackTrace: String?
}