package org.ebics.client.ebicsrestapi

import com.ninjasquad.springmockk.MockkBean
import org.ebics.client.api.trace.IFileService
import org.ebics.client.ebicsrestapi.bankconnection.UserServiceTestImpl
import org.ebics.client.ebicsrestapi.bankconnection.session.EbicsSessionFactory
import org.ebics.client.ebicsrestapi.bankconnection.session.IEbicsSessionFactory
import org.ebics.client.ebicsrestapi.configuration.EbicsRestConfiguration
import org.ebics.client.ebicsrestapi.utils.FileDownloadCache
import org.ebics.client.ebicsrestapi.utils.FileServiceMockImpl
import org.ebics.client.ebicsrestapi.utils.IFileDownloadCache
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@Lazy
class TestContext {

    @MockkBean
    private lateinit var configuration: EbicsRestConfiguration

    @Bean(name = ["testSessionFactory"])
    fun sessionFactory(): IEbicsSessionFactory = EbicsSessionFactory(
        UserServiceTestImpl(), configuration, EbicsProductConfiguration("testProd", "de", "JTO", "3.5")
    )

    @Bean
    fun servletWebServerFactory(): ServletWebServerFactory {
        return TomcatServletWebServerFactory()
    }

    @MockkBean
    private lateinit var fileDownloadH004: org.ebics.client.filetransfer.h004.FileDownload

    @MockkBean
    private lateinit var fileDownloadH005: org.ebics.client.filetransfer.h005.FileDownload

    @Bean(name = ["TestFileDownloadCache"])
    fun fileDownloadCache(): IFileDownloadCache = FileDownloadCache(fileService(), fileDownloadH004, fileDownloadH005)

    @Bean(name = ["FileServiceMockImpl"])
    fun fileService(): IFileService = FileServiceMockImpl()
}