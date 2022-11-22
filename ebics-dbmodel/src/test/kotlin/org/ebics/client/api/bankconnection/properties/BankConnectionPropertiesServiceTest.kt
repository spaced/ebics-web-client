package org.ebics.client.api.bankconnection.properties

import DbTestContext
import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bankconnection.BankConnection
import org.ebics.client.api.bankconnection.BankConnectionService
import org.ebics.client.model.EbicsVersion
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [DbTestContext::class])
class BankConnectionPropertiesServiceTest(
    @Autowired private val bankConnectionPropertyService: BankConnectionPropertyService,
    @Autowired private val bankConnectionService: BankConnectionService,
    @Autowired private val bankService: BankService
) {
    @Test
    @WithMockUser(username = "user_xxx", roles = ["USER"])
    fun createAndGetBankConnectionProperties() {
        val bank = BankData(URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH")
        val bankId = bankService.createBank(bank)
        val bankConnection = BankConnection(EbicsVersion.H004, "CHT10001", "Jan", "CH100001", bankId, false, false)
        val bankConnectionId = bankConnectionService.createBankConnection(bankConnection)
        val properties = bankConnectionPropertyService.findAllByBankConnectionId(bankConnectionId)
        Assertions.assertTrue(properties.isEmpty())
        val newProperties =
            listOf(
                BankConnectionPropertyUpdateRequest(null, "testKey1", "testVal1"),
                BankConnectionPropertyUpdateRequest(null, "testKey2", "testVal2")
            )
        val newPropertiesStored =
            bankConnectionPropertyService.updatePropertiesForBankConnectionId(bankConnectionId, newProperties)
        Assertions.assertEquals(newProperties.size, newPropertiesStored.size)

        val newPropertiesRetrieved = bankConnectionPropertyService.findAllByBankConnectionId(bankConnectionId)

        Assertions.assertEquals(newPropertiesStored.size, newPropertiesRetrieved.size)
        Assertions.assertEquals(newPropertiesStored, newPropertiesRetrieved)

        val updatedNewProperties2 = newPropertiesRetrieved.map { property -> BankConnectionPropertyUpdateRequest(property.id, "${property.key}-updated" , "${property.value}-updated") } +
                listOf(BankConnectionPropertyUpdateRequest(null, "new-key-1", "new-value-1"))

        val newPropertiesStored2 = bankConnectionPropertyService.updatePropertiesForBankConnectionId(bankConnectionId, updatedNewProperties2)

        Assertions.assertEquals(newPropertiesStored2.size, updatedNewProperties2.size)

        val newPropertiesRetrieved2 = bankConnectionPropertyService.findAllByBankConnectionId(bankConnectionId)

        Assertions.assertEquals(newPropertiesStored2.size, newPropertiesRetrieved2.size)
        Assertions.assertEquals(newPropertiesStored2, newPropertiesRetrieved2)
    }

}