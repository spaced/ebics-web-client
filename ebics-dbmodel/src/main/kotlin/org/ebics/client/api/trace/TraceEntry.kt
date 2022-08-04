package org.ebics.client.api.trace

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ebics.client.api.bank.Bank
import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.model.EbicsVersion
import java.time.ZonedDateTime
import javax.persistence.*

@Entity
data class TraceEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    /**
     * Text trace content (UFT-8), like XML files, text files,..
     */
    @Lob
    val textMessageBody:String?,

    /**
     * Binary trace content (like zip files, PDFs,..)
     */
    @Lob
    val binaryMessageBody:ByteArray?,

    /**
     * Reference to bank connection if given
     */
    @ManyToOne(optional = true)
    val bankConnection:BankConnectionEntity?,

    /**
     * Reference to bank if given
     */
    @ManyToOne(optional = true)
    val bank: Bank?,

    val sessionId: String,

    val orderNumber: String? = null,
    /**
     * The EBICS version used for this entry
     */
    val ebicsVersion: EbicsVersion,

    /**
     * Is this EBICS upload or download
     */
    val upload: Boolean,

    /**
     * Is this request operation or response operation
     */
    val request: Boolean,

    /**
     * Web user who created this entry
     */
    val creator: String = AuthenticationContext.fromSecurityContext().name,

    /**
     * Time when was the entry created
     */
    val dateTime: ZonedDateTime = ZonedDateTime.now(),

    /**
     * Optional Order Type of this entry
     *  (currently null by standard EBICS tracing)
     */
    @Embedded
    val orderType: OrderTypeDefinition? = null,

    val traceType: TraceType = TraceType.EbicsEnvelope,

    val traceCategory: TraceCategory = TraceCategory.ebicsOk,

    //Error relevant fields
    val errorCode: Int? = null,
    val errorCodeText: String? = null,
    val errorMessage: String? = null,
    val errorStackTrace: String? = null
) : TraceAccessRightsController {
    @JsonIgnore
    override fun getObjectName(): String = "Trace entry created by '$creator' from $dateTime"

    @JsonIgnore
    override fun getOwnerName(): String = creator
}
