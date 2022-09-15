package org.ebics.client.ebicsrestapi.bankconnection

import org.ebics.client.ebicsrestapi.EbicsRestApiApplication
import org.ebics.client.ebicsrestapi.TestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


@WebAppConfiguration
@WebMvcTest(controllers = [EbicsBankConnectionsResource::class])
@ContextConfiguration(classes = [TestContext::class, EbicsRestApiApplication::class])
@ExtendWith(RestDocumentationExtension::class, SpringExtension::class)
class EbicsBankConnectionsResourceTest(@Autowired private val context: WebApplicationContext) {

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(documentationConfiguration(restDocumentation)).build()
    }

    @Test
    fun bankConnectionsGet() {
        mockMvc.perform(get("/bankconnections"))
            .andExpect(status().isOk)
            .andDo(document("bankconnections"))
    }
}