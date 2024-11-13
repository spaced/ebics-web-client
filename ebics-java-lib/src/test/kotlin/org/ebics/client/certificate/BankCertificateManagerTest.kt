package org.ebics.client.certificate

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class BankCertificateManagerTest {

    @Test
    fun testCreateFromPubKeyExponentAndModulus() {

        // arrange
        val e002Exp = "AQAB".toByteArray()
        val x002Exp = "AQAB".toByteArray()
        val e002Mod = Base64.getDecoder().decode("sC/FhxiI5ssss+ysnbausffsfsb3PsAo1RpTLTy3QoUjHEh1nmpsqSTUUQdnkh3osfsfsfumljol1oXWD8HAoyv0D9Q3QihxengafyhphyeJXtMo0eAHZq9XDOWNNECYlkOafa7EM/mqR5X4ecfEiZwcI/astf4JCpyPlg1+kfMCPCk8kwCiFelmdakZJyK92zCirgBkiv9FkvK2/UW0XQ1JIQcfoXgQoj8vzfQOsrlVJYmRhItVFHnXjMLRsaSc/LDBN6eakOBGsJlzhF1/vVGt9OtQIEMtoy7ix5eC0fNSygJOp2ALOpOtfWLtAZ1AUJP0YomlGgYMk/EP82v0OlTtEjPs14qLNw==")
        val x002Mod = Base64.getDecoder().decode("zmmlMIpF+jXrQEejJAaL5RsfsfsfgZRMPQ56NACr1gjQU6Psfsf8/qasfsfs7FKbuep6UNr/l5aNUmpvl55rW+UhmAaaT8gjFOMQ32ZZdo/OjWkLXIOawj/j320jszvPJIZsbsff3cD/Cr/qAGDTr7+BR2l0vl8lug1xLGvyrc1D2JUqJ15HyEO0IUfU+MO+83Iu1Os4bDV448D3/dr4A0Z2jHVY83fY3TVmSJVSBWWG38qmX7kxccdjQ7BqPUDuLA0IFePkGJtZKNoHB2ObNBGKJCANpx9IchrPoP2hwcwG0usiNvn2RV3G3sIT6waV0OzOMZg/Ex7qT+y+rpUYu3uFAQ==")

        // act
        val result = BankCertificateManager.createFromPubKeyExponentAndModulus(e002Exp,e002Mod,x002Exp,x002Mod)

        // assert
        Assertions.assertEquals("1694B29A0C1790FD56DCC3CA567FC5CC903C4E1A14CE388045D140A7F5A03D83",String(result.e002Digest))

    }


}