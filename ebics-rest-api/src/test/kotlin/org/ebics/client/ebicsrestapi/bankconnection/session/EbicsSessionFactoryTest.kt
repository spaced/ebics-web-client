package org.ebics.client.ebicsrestapi.bankconnection.session

import io.mockk.junit5.MockKExtension
import org.ebics.client.api.bank.Bank
import org.ebics.client.ebicsrestapi.TestContext
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration


@SpringBootTest
@ExtendWith(MockKExtension::class)
@ContextConfiguration(classes = [TestContext::class])
class EbicsSessionFactoryTest(@Autowired private val ebicsSessionFactory: IEbicsSessionFactory) {

    @Test
    fun ifSessionRequestedWithWrongPwd_Then_ThrowIoException() {
        Assertions.assertThrows(Exception::class.java) {
            ebicsSessionFactory.getSession(
                UserIdPass(1, "WRONG_pass")
            )
        }
    }

    @Test
    fun ifSessionWithBankKeysRequestedAndEmptyCache_Then_NewSessionCreated() {
        with(
            ebicsSessionFactory.getSession(
                UserIdPass(1, "pass1")
            )
        ) {
            Assertions.assertNotNull(this)
            Assertions.assertTrue(sessionId.isNotBlank())
            Assertions.assertTrue(sessionId.length == 36)
            Assertions.assertNotNull(userCert)
            Assertions.assertNotNull(user)
            Assertions.assertNotNull((user.partner.bank as Bank).keyStore)
        }
    }

    @Test
    fun ifSessionWithoutBankKeysRequestedAndEmptyCache_Then_NewSessionCreated() {
        with(
            ebicsSessionFactory.getSession(
                UserIdPass(2, "pass2"), false
            )
        ) {
            Assertions.assertNotNull(this)
            Assertions.assertTrue(sessionId.isNotBlank())
            Assertions.assertTrue(sessionId.length == 36)
            Assertions.assertNotNull(sessionId)
            Assertions.assertNotNull(userCert)
            Assertions.assertNotNull(user)
            Assertions.assertNull((user.partner.bank as Bank).keyStore)
        }
    }

    @Test
    fun ifTwoSessionsRequested_Then_TwoDifferentIdsMustBeReturned() {
        val s1 =
            ebicsSessionFactory.getSession(
                UserIdPass(1, "pass1"), true
            )
        val s2 =
            ebicsSessionFactory.getSession(
                UserIdPass(2, "pass2"), false
            )

        Assertions.assertNotEquals(s1.sessionId, s2.sessionId, "Session ID must be unique")
    }

    //Session can't be cached, this must be done on DB layer (User, Partner, Bank entities),
    //otherwise would create lot of unnecessary dependencies how to evict the cache properly
    @Test
    fun ifTheNewSessionIsRequestedAndThenSameSessionOnceMore_Then_TheNewSessionIsReturned_NO_CACHE() {
        val s1 =
            ebicsSessionFactory.getSession(
                UserIdPass(1, "pass1"), true
            )
        val s2 =
            ebicsSessionFactory.getSession(
                UserIdPass(1, "pass1"), true
            )

        Assertions.assertNotEquals(s1.sessionId, s2.sessionId, "Session ID must different")
        //Assertions.assertEquals(s1, s2, "The returned session must be same")
    }

    @Test
    fun ifTheNewSessionIsRequestedAndThenSameSessionOnceMore_withWrongPwd_Then_IOException() {
        ebicsSessionFactory.getSession(
            UserIdPass(1, "pass1"), true
        )
        Assertions.assertThrows(Exception::class.java) {
            ebicsSessionFactory.getSession(
                UserIdPass(1, "pass1_WRONG"), true
            )
        }
    }
}