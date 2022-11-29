package org.ebics.client.api.healthstatus

import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.BankConnectionEntityInt
import org.ebics.client.api.bankconnection.cert.UserKeyStore
import org.ebics.client.api.bankconnection.properties.BankConnectionPropertyEntity
import org.ebics.client.api.partner.Partner
import org.ebics.client.api.trace.TraceEntry
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserStatusEnum

class BankConnectionWithHealthStatus(
    val id: Long? = null,
    override val partner: Partner,
    override val ebicsVersion: EbicsVersion,
    override val userId: String,
    override val name: String,
    override var dn: String,
    override var userStatus: EbicsUserStatusEnum,
    override val useCertificate: Boolean,
    override var usePassword: Boolean,
    override val creator: String,
    override val guestAccess: Boolean,
    keyStore: UserKeyStore?,
    traces: List<TraceEntry> = emptyList(),
    val backendStatus: ConnectionStatusDetail,
    //val properties: Map<String, String>,
    val properties: List<BankConnectionPropertyEntity>,
) : BankConnectionEntityInt {
    constructor(bankConnectionEntity: BankConnectionEntity, connectionStatus: ConnectionStatus) :
            this(
                bankConnectionEntity.id,
                bankConnectionEntity.partner,
                bankConnectionEntity.ebicsVersion,
                bankConnectionEntity.userId,
                bankConnectionEntity.name,
                bankConnectionEntity.dn,
                bankConnectionEntity.userStatus,
                bankConnectionEntity.useCertificate,
                bankConnectionEntity.usePassword,
                bankConnectionEntity.creator,
                bankConnectionEntity.guestAccess,
                bankConnectionEntity.keyStore,
                bankConnectionEntity.traces,
                connectionStatus.status,
                bankConnectionEntity.properties,
            )
}