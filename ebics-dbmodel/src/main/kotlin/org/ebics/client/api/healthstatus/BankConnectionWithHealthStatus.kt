package org.ebics.client.api.healthstatus

import org.ebics.client.api.EbicsPartner
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.BankConnectionEntityInt
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserStatusEnum

class BankConnectionWithHealthStatus(
    override val partner: EbicsPartner,
    override val ebicsVersion: EbicsVersion,
    override val userId: String,
    override val name: String,
    override val dn: String,
    override var userStatus: EbicsUserStatusEnum,
    override val useCertificate: Boolean,
    override val usePassword: Boolean,
    override val creator: String,
    override val guestAccess: Boolean,
    override val status: ConnectionStatusDetail,
) : BankConnectionEntityInt, ConnectionStatus {
    constructor(bankEntity: BankConnectionEntity, connectionStatus: ConnectionStatus) :
            this(
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
                connectionStatus.status,
            )
}