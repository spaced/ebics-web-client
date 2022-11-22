package org.ebics.client.api.bankconnection.properties

data class BankConnectionPropertyUpdateRequest (
    val id: Long? = null,
    val key: String,
    val value: String,
)