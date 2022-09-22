package org.ebics.client.ebicsrestapi

import com.ninjasquad.springmockk.MockkBean
import org.ebics.client.api.bankconnection.BankConnectionService
import org.ebics.client.ebicsrestapi.bankconnection.EbicsBankConnectionsResource
import org.ebics.client.ebicsrestapi.bankconnection.UserServiceTestImpl
import org.ebics.client.ebicsrestapi.configuration.EbicsRestConfiguration
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
    @MockkBean
    lateinit var configuration: EbicsRestConfiguration

    @Bean
    fun bankConnectionService(): BankConnectionService = UserServiceTestImpl()

    @Bean
    fun ebicsBankConnectionResource(): EbicsBankConnectionsResource = EbicsBankConnectionsResource(bankConnectionService())
}