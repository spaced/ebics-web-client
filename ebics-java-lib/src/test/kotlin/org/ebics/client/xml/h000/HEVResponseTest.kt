package org.ebics.client.xml.h000


import org.assertj.core.api.Assertions
import org.ebics.client.exception.EbicsException
import org.ebics.client.io.ByteArrayContentFactory
import org.junit.Assert
import org.junit.Test

class HEVResponseTest {


    @Test
    fun testSupportedVersions() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ebicsHEVResponse xsi:schemaLocation="http://www.ebics.org/H000 ebics_hev.xsd" xmlns="http://www.ebics.org/H000" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <SystemReturnCode>
                <ReturnCode>000000</ReturnCode>
                <ReportText>[EBICS_OK] OK</ReportText>
              </SystemReturnCode>
              <VersionNumber ProtocolVersion="H002">02.30</VersionNumber>
              <VersionNumber ProtocolVersion="H003">02.40</VersionNumber>
              <VersionNumber ProtocolVersion="H004">02.50</VersionNumber>
              <VersionNumber ProtocolVersion="H005">03.00</VersionNumber>
            </ebicsHEVResponse>
        """.trimIndent()

        val bacf = ByteArrayContentFactory(xml.toByteArray())
        val hr = HEVResponse(bacf).apply { build(); validate() }
        Assert.assertEquals(4, hr.getSupportedVersions().size)
    }

    @Test
    fun testValidateThrowsException() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ebicsHEVResponse xsi:schemaLocation="http://www.ebics.org/H000 ebics_hev.xsd" xmlns="http://www.ebics.org/H000" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <SystemReturnCode>
                <ReturnCode>000000</ReturnCode>
                <ReportText>[EBICS_OK] OK</ReportText>
              </SystemReturnCode>
              <X ProtocolVersion="H002">02.30</X>
            </ebicsHEVResponse>
        """.trimIndent()

        val bacf = ByteArrayContentFactory(xml.toByteArray())
        val hr = HEVResponse(bacf).apply { build() }

        Assert.assertThrows(EbicsException::class.java) { hr.validate() }
    }
}
