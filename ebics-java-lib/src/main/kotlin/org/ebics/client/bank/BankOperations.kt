package org.ebics.client.bank

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.trace.h000.BankTraceSession
import org.ebics.client.exception.EbicsException
import org.ebics.client.model.EbicsVersion
import java.io.IOException

interface BankOperations {
    @Throws(EbicsException::class, IOException::class)
    fun sendHEV(
        bank: EbicsBank,
        traceSession: BankTraceSession,
        configurationName: String = "default"
    ): List<EbicsVersion>
}