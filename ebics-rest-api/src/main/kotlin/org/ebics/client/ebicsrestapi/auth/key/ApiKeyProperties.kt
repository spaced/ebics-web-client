package org.ebics.client.ebicsrestapi.auth.key


import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * configuration properties for api keys
 * ebics.api.clients.firstAppId.key=aRandomKeyString
 * ebics.api.clients.firstAppId.role=admin
 */

@ConfigurationProperties(prefix = "ebics.api")
data class ApiKeyProperties(val clients: Map<String, ApiKey> = emptyMap()) {
    data class ApiKey(
        val key: String,
        val role: String = "api"
    )
}
