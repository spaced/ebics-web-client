package org.ebics.client.api.bank

import org.ebics.client.api.bank.cert.BankKeyStore

interface BankService {
    fun findBanks(): List<Bank>
    fun createBank(bank: BankData): Long
    fun updateBankById(bankId: Long, bank: BankData)
    fun deleteBankById(bankId: Long)
    fun getBankById(bankId: Long): Bank
    fun updateKeyStore(bank: Bank, bankKeyStore: BankKeyStore)
}