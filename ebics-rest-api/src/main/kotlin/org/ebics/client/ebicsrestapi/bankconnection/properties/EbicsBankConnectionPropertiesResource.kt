package org.ebics.client.ebicsrestapi.bankconnection.properties

import org.ebics.client.api.bankconnection.properties.BankConnectionPropertyEntity
import org.ebics.client.api.bankconnection.properties.BankConnectionPropertyService
import org.ebics.client.api.bankconnection.properties.BankConnectionPropertyUpdateRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("bankconnections/{bankConnectionId}/properties")
@CrossOrigin(origins = ["http://localhost:8081"])
class EbicsBankConnectionPropertiesResource(private val bankConnectionPropertyService: BankConnectionPropertyService) {

    @GetMapping
    fun getPropertiesForBankConnection(@PathVariable bankConnectionId: Long): List<BankConnectionPropertyEntity> {
        return bankConnectionPropertyService.findAllByBankConnectionId(bankConnectionId)
    }

    @PostMapping
    fun setPropertiesForBankConnection(
        @PathVariable bankConnectionId: Long,
        @RequestBody properties: List<BankConnectionPropertyUpdateRequest>
    ): List<BankConnectionPropertyEntity> {
        return bankConnectionPropertyService.updatePropertiesForBankConnectionId(bankConnectionId, properties)
    }
}