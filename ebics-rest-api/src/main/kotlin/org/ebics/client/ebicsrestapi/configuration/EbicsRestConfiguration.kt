package org.ebics.client.ebicsrestapi.configuration

import org.ebics.client.api.EbicsConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.*

@ConfigurationProperties("ebics")
data class EbicsRestConfiguration(
    override val signatureVersion: String = "A005",
    override val authenticationVersion: String = "X002",
    override val encryptionVersion: String = "E002",
    val trace: Boolean = true,
    val compression: Boolean = true,
    private val localeLanguage:String = "en",

) : EbicsConfiguration {
    override val locale: Locale = Locale.of(localeLanguage)
    override val isTraceEnabled = trace
    override val isCompressionEnabled = compression

    init {
        //Setting default locale as well in order to set locale for Messages singleton object
        Locale.setDefault(locale)
    }
}
