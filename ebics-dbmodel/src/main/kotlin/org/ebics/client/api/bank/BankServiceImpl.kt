package org.ebics.client.api.bank

import org.ebics.client.api.NotFoundException
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.getById
import org.springframework.stereotype.Service

@Service
class BankServiceImpl(private val bankRepository: BankRepository) : BankService {
    override fun findBanks(): List<Bank> = bankRepository.findAll()
    override fun createBank(bank: BankData): Long {
        val createdBank = Bank(null, bank.bankURL, bank.hostId, bank.name, null, emptyList(), bank.httpClientConfigurationName)
        bankRepository.saveAndFlush(createdBank)
        return createdBank.id!!
    }

    override fun updateBankById(bankId: Long, bank: BankData) {
        val currentBank = bankRepository.findById(bankId);
        when (currentBank.isPresent) {
            true -> {
                val updatedBank = Bank(bankId, bank.bankURL, bank.hostId, bank.name, currentBank.get().keyStore, currentBank.get().ebicsVersions, bank.httpClientConfigurationName)
                bankRepository.save(updatedBank)
            }
            false -> throw NotFoundException(bankId, "bank", null)
        }
    }

    override fun deleteBankById(bankId: Long) = bankRepository.deleteById(bankId)
    override fun getBankById(bankId: Long): Bank {
        return bankRepository.getById(bankId, "bank")
    }

    override fun updateKeyStore(bank: Bank, bankKeyStore: BankKeyStore) {
        bank.keyStore = bankKeyStore
        bankRepository.saveAndFlush(bank)
    }
}