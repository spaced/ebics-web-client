package org.ebics.client.api.trace

import DbTestContext
import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bankconnection.BankConnection
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.BankConnectionServiceImpl
import org.ebics.client.model.EbicsVersion
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL
import java.time.ZonedDateTime

@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [DbTestContext::class])
class TraceServiceTest(
    @Autowired private val userService: BankConnectionServiceImpl,
    @Autowired private val bankService: BankService,
    @Autowired private val traceRepository: TraceRepository,
) {
    private val traceService: TraceService = TraceService(traceRepository, true)

    @BeforeEach
    fun initTestContext() {
        traceRepository.deleteByDateTimeLessThan(ZonedDateTime.now())
    }

    private fun getMockBank(): Long {
        val bank = BankData(URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH")
        return bankService.createBank(bank)
    }

    private fun getMockUser(
        userId: String = "CHT10001",
        partnerId: String = "CH100001",
        bankId: Long = getMockBank()
    ): BankConnectionEntity {
        val userInfo = BankConnection(EbicsVersion.H004, userId, "Jan", partnerId, bankId, false, false)
        val bcId = userService.createUserAndPartner(userInfo)
        return userService.getUserById(bcId)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTraceServiceSearchForOwnEntries() {
        val mockUser1 = getMockUser()

        traceRepository.save(
            TraceEntry(
                null,
                "test",
                null,
                mockUser1,
                null,
                "sessId1",
                "O5N3",
                EbicsVersion.H004,
                false, false,
                creator = "jan"
            )
        )

        val result = traceService.findOwnTraces()

        Assertions.assertEquals(1, result.size)
        Assertions.assertTrue(result[0].javaClass.isAssignableFrom(TraceEntry::class.java))
        Assertions.assertFalse(result[0].javaClass.isAssignableFrom(AnonymousTraceEntry::class.java))
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTraceServiceSearchForOthersEntries() {
        val mockUser1 = getMockUser()

        traceRepository.save(
            TraceEntry(
                null,
                "test",
                null,
                mockUser1,
                null,
                "sessId1",
                "O5N3",
                EbicsVersion.H004,
                false, false,
                creator = "dominik"
            )
        )

        val result = traceService.findAllTraces()
        Assertions.assertEquals(1, result.size)
        Assertions.assertFalse(result[0].javaClass.isAssignableFrom(TraceEntry::class.java))
        Assertions.assertTrue(result[0].javaClass.isAssignableFrom(AnonymousTraceEntry::class.java))

        //Check if the creator is anonymized
        Assertions.assertNotEquals("dominik", result[0].creator)
    }


    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTraceServiceSearchForOwnAndOthersEntries() {
        val mockUser1 = getMockUser()

        traceRepository.save(
            TraceEntry(
                null,
                "test",
                null,
                mockUser1,
                null,
                "sessId1",
                "O5N3",
                EbicsVersion.H004,
                false, false,
                creator = "dominik"
            )
        )

        traceRepository.save(
            TraceEntry(
                null,
                "test",
                null,
                mockUser1,
                null,
                "sessId1",
                "O5N3",
                EbicsVersion.H004,
                false, false,
                creator = "jan"
            )
        )

        val result = traceService.findAllTraces()
        Assertions.assertEquals(2, result.size)
        Assertions.assertNotNull(result.singleOrNull { traceEntry -> traceEntry.javaClass.isAssignableFrom(TraceEntry::class.java)})
        Assertions.assertNotNull(result.singleOrNull { traceEntry -> traceEntry.javaClass.isAssignableFrom(AnonymousTraceEntry::class.java)})

        val result2 = traceService.findOwnTraces()
        Assertions.assertEquals(1, result2.size)
        Assertions.assertTrue(result2[0].javaClass.isAssignableFrom(TraceEntry::class.java))
    }
}