package org.ebics.client.http.factory

import java.net.URL

interface IHttpTransferSession {
    val ebicsUrl: URL
    val httpConfigurationName: String
}