package org.ebics.client.api.bankconnection.properties

import org.springframework.data.jpa.repository.JpaRepository

interface BankConnectionPropertyRepository : JpaRepository<BankConnectionPropertyEntity, Long> {
    fun findAllByBankConnectionId(bankConnectionId: Long): List<BankConnectionPropertyEntity>
}