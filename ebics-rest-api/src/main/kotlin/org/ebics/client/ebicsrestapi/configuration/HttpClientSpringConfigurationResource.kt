package org.ebics.client.ebicsrestapi.configuration

import org.ebics.client.http.factory.IHttpClientGlobalConfiguration
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("configuration/httpclient")
@CrossOrigin(origins = ["http://localhost:8081"])
class HttpClientSpringConfigurationResource(private val configuration: IHttpClientGlobalConfiguration) {
    @GetMapping()
    fun getUserSettings(): IHttpClientGlobalConfiguration {
        return configuration
    }
}

