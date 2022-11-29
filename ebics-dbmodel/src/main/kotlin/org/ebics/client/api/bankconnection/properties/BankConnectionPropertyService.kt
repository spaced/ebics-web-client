package org.ebics.client.api.bankconnection.properties

import org.ebics.client.api.bankconnection.BankConnectionRepository
import org.springframework.stereotype.Service

@Service
class BankConnectionPropertyService(
    private val bankConnectionPropertyRepository: BankConnectionPropertyRepository,
    private val bankConnectionRepository: BankConnectionRepository
) {
    fun findAllByBankConnectionId(bankConnectionId: Long): List<BankConnectionPropertyEntity> {
        return bankConnectionPropertyRepository.findAllByBankConnectionId(bankConnectionId)
    }

    fun updatePropertiesForBankConnectionId(
        bankConnectionId: Long,
        newProperties: List<BankConnectionPropertyUpdateRequest>
    ): List<BankConnectionPropertyEntity> {
        val currentProperties = bankConnectionPropertyRepository.findAllByBankConnectionId(bankConnectionId)
        //For all newProperties contained in the currentProperties, will be UPDATE of all values made
        //For all newProperties without id must be INSERT made
        val bankConnection = bankConnectionRepository.getById(bankConnectionId)
        val insertedAndUpdatedProperties = newProperties.map { property ->
            bankConnectionPropertyRepository.save(BankConnectionPropertyEntity.from(bankConnection, property))
        }

        //All newProperties not contained in the currentProperties must be DELETEd
        val newPropertyIds = newProperties.mapNotNull { it.id }
        currentProperties.filter { property -> !newPropertyIds.contains(property.id) }.map { property ->
            bankConnectionPropertyRepository.deleteById(property.id!!)
        }
        return insertedAndUpdatedProperties
    }
}