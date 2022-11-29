package org.ebics.client.http.factory

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.EbicsSession
import java.net.URL

data class HttpTransferSession(
    override val ebicsUrl: URL,
    override val httpConfigurationName: String
) : IHttpTransferSession {
    constructor(ebicsBank:EbicsBank) : this(ebicsBank.bankURL, ebicsBank.httpClientConfigurationName)
    constructor(ebicsSession: EbicsSession) : this(ebicsSession.user.partner.bank)
}
