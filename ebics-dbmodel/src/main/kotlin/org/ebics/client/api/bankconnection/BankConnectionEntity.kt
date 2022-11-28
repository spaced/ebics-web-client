package org.ebics.client.api.bankconnection

import com.fasterxml.jackson.annotation.JsonFilter
import com.fasterxml.jackson.annotation.JsonIgnore
import org.ebics.client.api.bankconnection.cert.UserKeyStore
import org.ebics.client.api.bankconnection.permission.BankConnectionAccessRightsController
import org.ebics.client.api.bankconnection.properties.BankConnectionPropertyEntity
import org.ebics.client.api.partner.Partner
import org.ebics.client.api.trace.TraceEntry
import org.ebics.client.model.EbicsVersion
import org.ebics.client.model.user.EbicsUserStatusEnum
import javax.persistence.*

@Entity
@JsonFilter("bankConnectionPropertiesFilter")
data class BankConnectionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    override val ebicsVersion: EbicsVersion,
    override val userId: String,
    override val name: String,
    override var dn: String,
    override var userStatus: EbicsUserStatusEnum = EbicsUserStatusEnum.CREATED,
    override val useCertificate: Boolean,
    override var usePassword: Boolean,

    @ManyToOne(optional = false)
    @JoinColumn(name = "PARTNER_ID")
    override val partner: Partner,

    @JsonIgnore
    @OneToOne(optional = true, cascade = [CascadeType.ALL])
    var keyStore: UserKeyStore?,

    override val creator: String,
    override val guestAccess: Boolean,

    @JsonIgnore
    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "bankConnection")
    val traces: List<TraceEntry> = emptyList(),

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "bankConnection")
    val properties: List<BankConnectionPropertyEntity> = emptyList(),

    ) : BankConnectionEntityInt, BankConnectionAccessRightsController {
    @JsonIgnore
    override fun getCreatorName(): String = creator
    override fun isGuestAccess(): Boolean = guestAccess

    @JsonIgnore
    override fun getObjectName(): String = "BankConnection: '$name' created by: '$creator' of EBICS user id: '$userId'"
}