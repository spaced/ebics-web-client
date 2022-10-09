package org.ebics.client.ebicsrestapi.bank

import org.ebics.client.api.EbicsBank
import org.ebics.client.api.trace.h000.BankTraceSession
import org.ebics.client.bank.BankOperations
import org.ebics.client.model.EbicsVersion

class BankOperationsTestImpl : BankOperations {
    override fun sendHEV(
        bank: EbicsBank,
        traceSession: BankTraceSession,
        configurationName: String
    ): List<EbicsVersion> {
        TODO("Not yet implemented")
    }
}