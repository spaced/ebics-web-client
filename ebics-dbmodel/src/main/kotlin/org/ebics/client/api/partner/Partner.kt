package org.ebics.client.api.partner

import com.fasterxml.jackson.annotation.JsonFilter
import org.ebics.client.api.EbicsPartner
import org.ebics.client.api.bank.Bank
import javax.persistence.*

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["partnerId", "bank_id"])])
@JsonFilter("partnerPropertiesFilter")
data class Partner(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    @ManyToOne(optional = false)
    override val bank: Bank,

    override val partnerId: String,
    override var orderId: Int) : EbicsPartner
