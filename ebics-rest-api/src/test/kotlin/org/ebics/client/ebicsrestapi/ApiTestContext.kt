package org.ebics.client.ebicsrestapi

import com.ninjasquad.springmockk.MockkBean
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.versions.VersionSupportService
import org.ebics.client.api.bankconnection.BankConnectionService
import org.ebics.client.api.healthstatus.HealthStatusEnrichmentService
import org.ebics.client.bank.BankOperations
import org.ebics.client.ebicsrestapi.bank.BankOperationsTestImpl
import org.ebics.client.ebicsrestapi.bank.BankServiceTestImpl
import org.ebics.client.ebicsrestapi.bank.EbicsBankAPI
import org.ebics.client.ebicsrestapi.bank.EbicsBankResource
import org.ebics.client.ebicsrestapi.bank.versions.VersionSupportServiceTestImpl
import org.ebics.client.ebicsrestapi.bankconnection.BankConnectionServiceTestImpl
import org.ebics.client.ebicsrestapi.bankconnection.EbicsBankConnectionsResource
import org.ebics.client.ebicsrestapi.configuration.EbicsRestConfiguration
import org.ebics.client.ebicsrestapi.healthstatus.HealthStatusEnrichmentServiceTestImpl
import org.ebics.client.ebicsrestapi.utils.restfilter.JsonFilterProviderHelper
import org.ebics.client.ebicsrestapi.utils.restfilter.RestFilterAdvice
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

/**
 * This test context is used for REST API endpoint testing
 * The data itself provided to web layer are here mocked so that we can compare with expectations
 */
@Configuration
@Lazy
class ApiTestContext {

    @Bean
    fun bankConnectionService(): BankConnectionService = BankConnectionServiceTestImpl()

    @Bean
    fun filterProviderHelper() : JsonFilterProviderHelper = JsonFilterProviderHelper()

    @Bean
    fun restFilterAdvice() : RestFilterAdvice = RestFilterAdvice(filterProviderHelper())

    @Bean
    fun bankService(): BankService = BankServiceTestImpl()

    @Bean
    fun versionSupportService(): VersionSupportService = VersionSupportServiceTestImpl()

    @Bean
    fun healthStatusEnrichmentService(): HealthStatusEnrichmentService = HealthStatusEnrichmentServiceTestImpl()

    @Bean
    fun bankOperations(): BankOperations = BankOperationsTestImpl()

    @Bean
    fun ebicsBankAPI(): EbicsBankAPI = EbicsBankAPI(bankService(), versionSupportService(), bankOperations())

    @Bean
    fun ebicsBankConnectionResource(): EbicsBankConnectionsResource = EbicsBankConnectionsResource(bankConnectionService(), healthStatusEnrichmentService())

    @Bean
    fun ebicsBankResource(): EbicsBankResource = EbicsBankResource(bankService(), ebicsBankAPI())
}