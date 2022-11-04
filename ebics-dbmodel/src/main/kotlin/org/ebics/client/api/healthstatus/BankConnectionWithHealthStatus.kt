package org.ebics.client.api.healthstatus

import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.BankConnectionEntityInt
import org.ebics.client.api.bankconnection.cert.UserKeyStore
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
) : BankConnectionEntityInt {
    constructor(bankEntity: BankConnectionEntity, connectionStatus: ConnectionStatus) :
            this(
                bankEntity.id,
                bankEntity.partner,
                bankEntity.ebicsVersion,
                bankEntity.userId,
                bankEntity.name,
                bankEntity.dn,
                bankEntity.userStatus,
                bankEntity.useCertificate,
                bankEntity.usePassword,
                bankEntity.creator,
                bankEntity.guestAccess,
                bankEntity.keyStore,
                bankEntity.traces,
                connectionStatus.status,
            )
}