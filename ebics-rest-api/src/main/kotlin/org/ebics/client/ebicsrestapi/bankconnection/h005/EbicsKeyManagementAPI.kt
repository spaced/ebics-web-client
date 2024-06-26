package org.ebics.client.ebicsrestapi.bankconnection.h005

import org.ebics.client.api.bank.BankService
import org.ebics.client.api.bank.cert.BankKeyStore
import org.ebics.client.api.bank.cert.BankKeyStoreService
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.BankConnectionService
import org.ebics.client.api.trace.BankConnectionTraceSession
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.bankconnection.session.IEbicsSessionFactory
import org.ebics.client.keymgmt.h005.KeyManagement
import org.ebics.client.order.EbicsAdminOrderType
import org.springframework.stereotype.Component

@Component("EbicsKeyManagementAPIH005")
class EbicsKeyManagementAPI(
    private val bankConnectionService: BankConnectionService,
    private val bankService: BankService,
    private val bankKeyStoreService: BankKeyStoreService,
    private val sessionFactory: IEbicsSessionFactory,
    private val keyManagement: KeyManagement
) {

    fun sendINI(userIdPass: UserIdPass) {
        val session = sessionFactory.getSession(userIdPass, false)
        val traceSession = BankConnectionTraceSession(session, OrderTypeDefinition(EbicsAdminOrderType.INI), true)
        keyManagement.sendINI(session, traceSession)
        //The state of user was changed after INI, must be persisted
        bankConnectionService.saveBankConnection(session.user as BankConnectionEntity)
    }

    fun sendHIA(userIdPass: UserIdPass) {
        val session = sessionFactory.getSession(userIdPass, false)
        val traceSession = BankConnectionTraceSession(session, OrderTypeDefinition(EbicsAdminOrderType.HIA), true)
        keyManagement.sendHIA(session, traceSession)
        //The state of user was changed after HIA, must be persisted
        bankConnectionService.saveBankConnection(session.user as BankConnectionEntity)
    }

    fun sendHPB(userIdPass: UserIdPass) {
        val session = sessionFactory.getSession(userIdPass, false)
        val traceSession = BankConnectionTraceSession(session, OrderTypeDefinition(EbicsAdminOrderType.HPB), false)
        val bankCertManager = keyManagement.sendHPB(session, traceSession, userIdPass.password)
        val user = session.user as BankConnectionEntity
        val bankKeyStore = BankKeyStore.fromBankCertMgr(bankCertManager, user.partner.bank)
        bankKeyStoreService.save(bankKeyStore) //BankKeyStore must be saved
        bankService.updateKeyStore(user.partner.bank, bankKeyStore) //BankKeyStore must be added to bank
        bankConnectionService.saveBankConnection(user) //The state of user was changed after HPB, must be persisted
    }
}