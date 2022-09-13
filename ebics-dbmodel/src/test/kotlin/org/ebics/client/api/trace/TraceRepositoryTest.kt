package org.ebics.client.api.trace

import DbTestContext
import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bankconnection.BankConnection
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.BankConnectionServiceImpl
import org.ebics.client.api.trace.orderType.EbicsMessage
import org.ebics.client.api.trace.orderType.EbicsService
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.EbicsAdminOrderType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL
import java.time.ZoneId
import java.time.ZonedDateTime

@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [DbTestContext::class])
class TraceRepositoryTest(
    @Autowired private val userService: BankConnectionServiceImpl,
    @Autowired private val bankService: BankService,
    @Autowired private val traceRepository: TraceRepository,
) {
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
        val bcId = userService.createBankConnection(userInfo)
        return userService.getBankConnectionById(bcId)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByCreator() {
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
        val result = traceRepository.findOne(creatorEquals("jan"))
        Assertions.assertTrue(result.isPresent)
        val result2 = traceRepository.findOne(creatorEquals("chosee"))
        Assertions.assertFalse(result2.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByUser() {
        val bankId = getMockBank()
        val mockUser1 = getMockUser("CHT001", "CH1", bankId)
        val mockUser2 = getMockUser("CHT002", "CH1", bankId)
        val mockUser3 = getMockUser("CHT002", "XXXXX", bankId)

        traceRepository.save(
            TraceEntry(
                null,
                "test", null,
                mockUser1,
                mockUser1.partner.bank,
                "sessId1",
                "O5N3",
                EbicsVersion.H004,
                false,
                true,
                creator = "jan"
            )
        )
        val result = traceRepository.findOne(bankConnectionEquals(mockUser1))
        Assertions.assertTrue(result.isPresent)
        val result2 = traceRepository.findOne(bankConnectionEquals(mockUser2, false))
        Assertions.assertFalse(result2.isPresent)
        val result3 = traceRepository.findOne(bankConnectionEquals(mockUser2, true))
        Assertions.assertTrue(result3.isPresent)
        val result4 = traceRepository.findOne(bankConnectionEquals(mockUser3, true))
        Assertions.assertFalse(result4.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByCreatorAndOrderType() {
        val mockUser1 = getMockUser()
        traceRepository.save(
            TraceEntry(
                null,
                "test",
                null,
                mockUser1,
                null,
                "sessId1", "O5N3", EbicsVersion.H004, false, false, creator = "jan",
                orderType = OrderTypeDefinition(EbicsAdminOrderType.HTD)
            )
        )

        val negativeResult =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HAC)))
        Assertions.assertFalse(negativeResult.isPresent)

        val result =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD)))
        Assertions.assertTrue(result.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByCreatorAndOrderTypeExt() {
        val mockUser1 = getMockUser()
        traceRepository.save(
            TraceEntry(
                null, "test", null, mockUser1, null, "sessId1", "O5N3", EbicsVersion.H004, false, false, creator = "jan", orderType =
                OrderTypeDefinition(EbicsAdminOrderType.HTD, null, "XE2")
            )
        )

        val negativeResult =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HAC)))
        Assertions.assertFalse(negativeResult.isPresent)

        val negativeResult2 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD, null, "XXX")))
        Assertions.assertFalse(negativeResult2.isPresent)

        val posResult1 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD)))
        Assertions.assertTrue(posResult1.isPresent)

        val posResult2 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD, null, "XE2")))
        Assertions.assertTrue(posResult2.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByCreatorAndBtf() {
        val mockUser1 = getMockUser()
        val service = EbicsService("name", "s", "dd", message = EbicsMessage("name1", "ff", "001", "Zip"))
        traceRepository.save(
            TraceEntry(
                null, "test", null, mockUser1, null, "sessId1", "O5N3", EbicsVersion.H004, false, false, creator = "jan", orderType =
                OrderTypeDefinition(EbicsAdminOrderType.HTD, service)
            )
        )

        val negativeResult =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HAC)))
        Assertions.assertFalse(negativeResult.isPresent)

        val negativeResult2 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD, null, "XXX")))
        Assertions.assertFalse(negativeResult2.isPresent)

        val posResult1 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD)))
        Assertions.assertTrue(posResult1.isPresent)

        val posResult2 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD, service)))
        Assertions.assertTrue(posResult2.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByEbicsVersionBtf() {
        val mockUser1 = getMockUser()
        val service = EbicsService("name", "s", "dd", message = EbicsMessage("name1", "ff", "001", "Zip"))
        traceRepository.save(
            TraceEntry(
                null, "test", null, mockUser1,  null,"sessId1", "O5N3", EbicsVersion.H004, false, false, creator = "jan", orderType =
                OrderTypeDefinition(EbicsAdminOrderType.HTD, service)
            )
        )

        val negativeResult =
            traceRepository.findOne(ebicsVersionEquals(EbicsVersion.H005))
        Assertions.assertFalse(negativeResult.isPresent)

        val positiveResult =
            traceRepository.findOne(ebicsVersionEquals(EbicsVersion.H004))
        Assertions.assertTrue(positiveResult.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByMessageBody() {
        val mockUser1 = getMockUser()
        val service = EbicsService("name", "s", "dd", message = EbicsMessage("name1", "ff", "001", "Zip"))
        traceRepository.save(
            TraceEntry(
                null, "test", null, mockUser1,  null,"sessId1", "O5N3", EbicsVersion.H004, false, false, creator = "jan", orderType =
                OrderTypeDefinition(EbicsAdminOrderType.HTD, service)
            )
        )

        val positiveResult =
            traceRepository.findOne(traceMessageBodyIsNotEmpty())
        Assertions.assertTrue(positiveResult.isPresent)

        val negativeResult =
            traceRepository.findOne(Specification.not(traceMessageBodyIsNotEmpty()))
        Assertions.assertFalse(negativeResult.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByMessageBodyNull() {
        val mockUser1 = getMockUser()
        val service = EbicsService("name", "s", "dd", message = EbicsMessage("name1", "ff", "001", "Zip"))
        traceRepository.save(
            TraceEntry(
                null, null, null, mockUser1,  null,"sessId1", "O5N3", EbicsVersion.H004, false, false, creator = "jan", orderType =
                OrderTypeDefinition(EbicsAdminOrderType.HTD, service)
            )
        )

        val positiveResult =
            traceRepository.findOne(traceMessageBodyIsNotEmpty())
        Assertions.assertFalse(positiveResult.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByTraceCategory() {
        val mockUser1 = getMockUser()
        val service = EbicsService("name", "s", "dd", message = EbicsMessage("name1", "ff", "001", "Zip"))
        traceRepository.save(
            TraceEntry(
                null, "test", null, mockUser1,  null,"sessId1", "O5N3", EbicsVersion.H004, false, false, creator = "jan", orderType =
                OrderTypeDefinition(EbicsAdminOrderType.HTD, service),
                traceCategory = TraceCategory.EbicsResponseError
            )
        )

        val positiveResult =
            traceRepository.findOne(traceCategoryEquals(TraceCategory.EbicsResponseError))
        Assertions.assertTrue(positiveResult.isPresent)

        val negativeResult =
            traceRepository.findOne(traceCategoryEquals(TraceCategory.EbicsResponseOk)
                .or(traceCategoryEquals(TraceCategory.HttpResponseError))
                .or(traceCategoryEquals(TraceCategory.HttpResponseOk)))
        Assertions.assertFalse(negativeResult.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoGetHealthStatistics() {
        val mockUser1 = getMockUser()
        val service = EbicsService("name", "s", "dd", message = EbicsMessage("name1", "ff", "001", "Zip"))
        traceRepository.save(
            TraceEntry(
                null, "test", null, mockUser1,  null,"sessId1", "O5N3", EbicsVersion.H004, false, false, creator = "jan", orderType =
                OrderTypeDefinition(EbicsAdminOrderType.HTD, service),
                traceCategory = TraceCategory.EbicsResponseError
            )
        )

        traceRepository.save(
            TraceEntry(
                null, "test", null, mockUser1,  null,"sessId1", "O5N3", EbicsVersion.H004, false, false, creator = "jan", orderType =
                OrderTypeDefinition(EbicsAdminOrderType.HTD, service),
                traceCategory = TraceCategory.HttpResponseError
            )
        )

        traceRepository.save(
            TraceEntry(
                null, "test", null, mockUser1,  null,"sessId1", "O5N3", EbicsVersion.H004, false, false, creator = "jan", orderType =
                OrderTypeDefinition(EbicsAdminOrderType.HTD, service),
                traceCategory = TraceCategory.EbicsResponseOk
            )
        )

        val fixTimeInPast = ZonedDateTime.of(2022,2,2,0,0,0,0, ZoneId.systemDefault())
        val errorCount =
            traceRepository.getTraceEntryCountByBankConnectionIdAndTraceCategoryIn(mockUser1.id!!, fixTimeInPast, setOf(TraceCategory.EbicsResponseError, TraceCategory.HttpResponseError))
        val okCount =
            traceRepository.getTraceEntryCountByBankConnectionIdAndTraceCategoryIn(mockUser1.id!!, fixTimeInPast, setOf(TraceCategory.EbicsResponseOk))

        Assertions.assertEquals(2, errorCount)
        Assertions.assertEquals(1, okCount)

        val result = traceRepository.getTraceEntryCountForTraceCategoryInGroupedByBankConnectionId(fixTimeInPast, setOf(TraceCategory.EbicsResponseError, TraceCategory.HttpResponseError))
        Assertions.assertNotNull(result)
        Assertions.assertEquals(1, result.size)
        Assertions.assertEquals(2, result[0].traceEntryCount)
        Assertions.assertEquals(mockUser1.id!!, result[0].bankConnectionId)
    }
}