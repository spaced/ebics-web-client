package org.ebics.client.ebicsrestapi.bank

import org.ebics.client.api.NotFoundException
import org.ebics.client.api.bank.Bank
import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.ebicsrestapi.MockObjectHelper

class BankServiceTestImpl : BankService {
    private val mockBanks = mapOf(
        1L to MockObjectHelper.createMockBank(1, "Test bank 1"),
        2L to MockObjectHelper.createMockBank(2, "Test bank 2")
    )

    override fun findBanks(): List<Bank> {
        return mockBanks.values.toList()
    }

    override fun createBank(bank: BankData): Long {
        TODO("Not yet implemented")
    }

    override fun updateBankById(bankId: Long, bank: BankData) {
        TODO("Not yet implemented")
    }

    override fun deleteBankById(bankId: Long) {
        TODO("Not yet implemented")
    }

    override fun getBankById(bankId: Long): Bank {
        return mockBanks[bankId] ?: throw NotFoundException(1, "bank", null)
    }

    override fun updateKeyStore(bank: Bank, bankKeyStore: BankKeyStore) {
        TODO("Not yet implemented")
    }
}