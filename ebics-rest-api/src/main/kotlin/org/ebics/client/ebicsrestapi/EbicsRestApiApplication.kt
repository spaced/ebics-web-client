package org.ebics.client.ebicsrestapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.io.File

@SpringBootApplication
@ComponentScan(
    "org.ebics.client.api",
    "org.ebics.client.ebicsrestapi",
    "org.ebics.client.bank",
    "org.ebics.client.filetransfer",
    "org.ebics.client.keymgmt",
    "org.ebics.client.http.factory"
)
@EntityScan("org.ebics.client.api")
@EnableJpaRepositories("org.ebics.client.api")
class EbicsRestApiApplication : SpringBootServletInitializer() {

    /**
     * Start app as war file
     */
    override fun configure(builder: SpringApplicationBuilder): SpringApplicationBuilder {
        return builder.sources(EbicsRestApiApplication::class.java)
    }

}

fun main(args: Array<String>) {
    runApplication<EbicsRestApiApplication>(*args)
}

