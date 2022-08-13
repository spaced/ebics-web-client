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
    override val id:Long? = null,

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
    override val bankConnection:BankConnectionEntity?,

    /**
     * Reference to bank if given
     */
    @ManyToOne(optional = true)
    override val bank: Bank?,

    val sessionId: String,

    val orderNumber: String? = null,
    /**
     * The EBICS version used for this entry
     */
    override val ebicsVersion: EbicsVersion,

    /**
     * Is this EBICS upload or download
     */
    override val upload: Boolean,

    /**
     * Is this request operation or response operation
     */
    override val request: Boolean,

    /**
     * Web user who created this entry
     */
    override val creator: String = AuthenticationContext.fromSecurityContext().name,

    /**
     * Time when was the entry created
     */
    override val dateTime: ZonedDateTime = ZonedDateTime.now(),

    /**
     * Optional Order Type of this entry
     *  (currently null by standard EBICS tracing)
     */
    @Embedded
    override val orderType: OrderTypeDefinition? = null,

    override val traceType: TraceType = TraceType.EbicsEnvelope,

    override val traceCategory: TraceCategory = TraceCategory.Request,

    //Error relevant fields
    override val errorCode: String? = null,
    override val errorCodeText: String? = null,
    override val errorMessage: String? = null,
    override val errorStackTrace: String? = null
) : TraceAccessRightsController, BaseTraceEntry {
    @JsonIgnore
    override fun getObjectName(): String = "Trace entry created by '$creator' from $dateTime"

    @JsonIgnore
    override fun getOwnerName(): String = creator
}
