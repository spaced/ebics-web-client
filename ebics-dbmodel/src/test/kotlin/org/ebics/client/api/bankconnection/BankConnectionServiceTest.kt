package org.ebics.client.api.bankconnection

import DbTestContext
import org.assertj.core.api.Assertions.assertThat
import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankServiceImpl
import org.ebics.client.model.EbicsVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URI

@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [DbTestContext::class])
class BankConnectionServiceTest(
    @Autowired private val bankConnectionService: BankConnectionService,
    @Autowired private val bankService: BankServiceImpl,
) {
    @Test
    @WithMockUser(username = "user_xxx", roles = ["USER"])
    fun createAndGetBankConnection() {
        val bank = BankData(  URI("https://ebics.ubs.com/ebicsweb/ebicsweb").toURL(),  "EBXUBSCH", "UBS-PROD-CH")
        val bankId = bankService.createBank(bank)
        val bankConnection = BankConnection(EbicsVersion.H004, "CHT10001", "Jan",  "CH100001", bankId, false, false)
        val bankConnectionId = bankConnectionService.createBankConnection(bankConnection)
        with( bankConnectionService.getBankConnectionById(bankConnectionId) ) {
            assertThat( name ).isEqualTo( bankConnection.name )
            assertThat( partner.bank.bankURL ).isEqualTo( bank.bankURL )
            assertThat( ebicsVersion ).isEqualTo( bankConnection.ebicsVersion )
            assertThat( partner.partnerId ).isEqualTo("CH100001")
        }
    }
}
