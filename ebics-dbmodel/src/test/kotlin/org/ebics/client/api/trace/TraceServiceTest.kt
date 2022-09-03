package org.ebics.client.api.trace

import DbTestContext
import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bankconnection.BankConnection
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.BankConnectionServiceImpl
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.exception.EbicsServerException
import org.ebics.client.exception.HttpServerException
import org.ebics.client.exception.h005.EbicsReturnCode
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.EbicsAdminOrderType
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
        Assertions.assertNotNull(result.singleOrNull { traceEntry -> traceEntry.javaClass.isAssignableFrom(TraceEntry::class.java) })
        Assertions.assertNotNull(result.singleOrNull { traceEntry ->
            traceEntry.javaClass.isAssignableFrom(
                AnonymousTraceEntry::class.java
            )
        })

        val result2 = traceService.findOwnTraces()
        Assertions.assertEquals(1, result2.size)
        Assertions.assertTrue(result2[0].javaClass.isAssignableFrom(TraceEntry::class.java))
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun whenEbicsEnvelopeTraced_thenTheEbicsEnvelopeRecordToBeFound() {
        val mockUser1 = getMockUser()
        val orderTypeDefinition = OrderTypeDefinition(adminOrderType = EbicsAdminOrderType.BTD)
        val traceSession = BankConnectionTraceSession(
            mockUser1,
            orderType = orderTypeDefinition,
            upload = true,
            request = true,
            "sessionId1"
        )
        val testInput = "testäöä\$ü\u8921'"
        traceService.trace(ByteArrayContentFactory(testInput.toByteArray()), traceSession)

        val result = traceService.findAllTraces()
        Assertions.assertEquals(1, result.size)
        Assertions.assertTrue(result[0].javaClass.isAssignableFrom(TraceEntry::class.java))
        with(result[0] as TraceEntry) {
            Assertions.assertEquals(testInput, textMessageBody)
            Assertions.assertArrayEquals(testInput.toByteArray(), binaryMessageBody)
            Assertions.assertTrue(request) //Should NOT be overwritten for standard trace case
            Assertions.assertTrue(upload)
            Assertions.assertEquals(mockUser1.partner.bank.bankURL, bank?.bankURL)
            Assertions.assertEquals(mockUser1.partner.partnerId, bankConnection?.partner?.partnerId)
            Assertions.assertEquals("sessionId1", sessionId)
            Assertions.assertEquals(TraceType.EbicsEnvelope, traceType)
            Assertions.assertEquals(TraceCategory.Request, traceCategory)
            Assertions.assertNull(errorMessage)
            Assertions.assertNull(errorCode)
            Assertions.assertNull(errorCodeText)
            Assertions.assertTrue(errorStackTrace.isNullOrBlank())
        }
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun whenEbicsExceptionTraced_thenTheEbicsExceptionRecordToBeFound() {
        val mockUser1 = getMockUser()
        val traceSession = BankConnectionTraceSession(
            mockUser1,
            OrderTypeDefinition(adminOrderType = EbicsAdminOrderType.BTD),
            true,
            request = false,
            "sessionId1"
        )
        traceService.traceException(
            EbicsServerException(EbicsReturnCode.EBICS_NO_DOWNLOAD_DATA_AVAILABLE),
            traceSession
        )

        val result = traceService.findAllTraces()
        Assertions.assertEquals(1, result.size)
        Assertions.assertTrue(result[0].javaClass.isAssignableFrom(TraceEntry::class.java))
        with(result[0] as TraceEntry) {
            Assertions.assertNull(textMessageBody)
            Assertions.assertNull(binaryMessageBody)
            Assertions.assertFalse(request) //Should be overwritten for exception to false
            Assertions.assertTrue(upload)
            Assertions.assertEquals(mockUser1.partner.bank.bankURL, bank?.bankURL)
            Assertions.assertEquals(mockUser1.partner.partnerId, bankConnection?.partner?.partnerId)
            Assertions.assertEquals("sessionId1", sessionId)
            Assertions.assertEquals(TraceType.EbicsEnvelope, traceType)
            Assertions.assertEquals(TraceCategory.EbicsResponseError, traceCategory)
            Assertions.assertEquals(EbicsReturnCode.EBICS_NO_DOWNLOAD_DATA_AVAILABLE.toString(), errorMessage)
            Assertions.assertEquals(EbicsReturnCode.EBICS_NO_DOWNLOAD_DATA_AVAILABLE.code, errorCode)
            Assertions.assertEquals(EbicsReturnCode.EBICS_NO_DOWNLOAD_DATA_AVAILABLE.text, errorCodeText)
            Assertions.assertFalse(errorStackTrace.isNullOrBlank())
        }
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun whenHttpExceptionTraced_thenTheHttpExceptionRecordToBeFound() {
        val mockUser1 = getMockUser()
        val traceSession = BankConnectionTraceSession(
            mockUser1,
            OrderTypeDefinition(adminOrderType = EbicsAdminOrderType.BTD),
            false,
            request = true,
            "sessionId1"
        )
        traceService.traceException(HttpServerException("503", "Error 503", "err 503"), traceSession)

        val result = traceService.findAllTraces()
        Assertions.assertEquals(1, result.size)
        Assertions.assertTrue(result[0].javaClass.isAssignableFrom(TraceEntry::class.java))
        with(result[0] as TraceEntry) {
            Assertions.assertNull(textMessageBody)
            Assertions.assertNull(binaryMessageBody)
            Assertions.assertFalse(request) //Should be overwritten for exception to false
            Assertions.assertFalse(upload)
            Assertions.assertEquals(mockUser1.partner.bank.bankURL, bank?.bankURL)
            Assertions.assertEquals(mockUser1.partner.partnerId, bankConnection?.partner?.partnerId)
            Assertions.assertEquals("sessionId1", sessionId)
            Assertions.assertEquals(TraceType.EbicsEnvelope, traceType)
            Assertions.assertEquals(TraceCategory.HttpResponseError, traceCategory)
            Assertions.assertEquals("err 503", errorMessage)
            Assertions.assertEquals("503", errorCode)
            Assertions.assertEquals("Error 503", errorCodeText)
            Assertions.assertFalse(errorStackTrace.isNullOrBlank())
        }
    }
}