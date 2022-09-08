package org.ebics.client.api.bankconnection

import org.ebics.client.api.bankconnection.permission.BankConnectionAccessType

interface BankConnectionService {
    fun findBankConnections(permission: BankConnectionAccessType): List<BankConnectionEntity>
    fun getBankConnectionById(bankConnectionId: Long, permission: BankConnectionAccessType = BankConnectionAccessType.READ): BankConnectionEntity
    fun saveBankConnection(bankConnection: BankConnectionEntity): Long
    fun createBankConnection(bankConnection: BankConnection): Long
    fun updateBankConnection(bankConnectionId: Long, bankConnection: BankConnection): Long
    fun deleteBankConnection(bankConnectionId: Long)

    /**
     * Resetting user status to default
     * After such reset must be user newly initialized, including creation of user keys
     */
    fun resetStatus(bankConnectionId: Long): Unit
}