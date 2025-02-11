package org.ebics.client.api.bankconnection

import org.ebics.client.api.bankconnection.cert.UserKeyStoreService
import org.ebics.client.api.bankconnection.permission.BankConnectionAccessType
import org.ebics.client.api.getById
import org.ebics.client.api.partner.Partner
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.api.security.AuthenticationContext
import org.ebics.client.model.user.EbicsUserAction
import org.ebics.client.model.user.EbicsUserStatusEnum
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class BankConnectionServiceImpl(
    private val bankConnectionRepository: BankConnectionRepository,
    private val partnerService: PartnerService,
    private val userKeyStoreService: UserKeyStoreService,
) : BankConnectionService {

    override fun findBankConnections(permission: BankConnectionAccessType): List<BankConnectionEntity> {
        return bankConnectionRepository.findAll().filter { it.hasAccess(permission, AuthenticationContext.fromSecurityContext()) }
    }

    override fun getBankConnectionById(bankConnectionId: Long, permission: BankConnectionAccessType): BankConnectionEntity {
        val bankConnection = bankConnectionRepository.getById(bankConnectionId, "bankconnection")
        bankConnection.checkAccess(permission)
        return bankConnection
    }

    override fun saveBankConnection(bankConnection: BankConnectionEntity): Long {
        bankConnection.checkWriteAccess()
        bankConnectionRepository.saveAndFlush(bankConnection)
        return bankConnection.id!!
    }

    override fun createBankConnection(bankConnection: BankConnection): Long {
        with(bankConnection) {
            val partner = partnerService.createOrGetPartner(partnerId, bankId)
            val authCtx = AuthenticationContext.fromSecurityContext()
            val user = BankConnectionEntity(
                null,
                ebicsVersion,
                userId,
                name,
                dn = DomainNameGenerator(name, Locale.getDefault()?.country).toString(),
                useCertificate = useCertificate,
                usePassword = true,
                partner = partner,
                keyStore = null,
                creator = authCtx.name,
                guestAccess = guestAccess
            )
            user.checkWriteAccess()
            bankConnectionRepository.saveAndFlush(user)
            return user.id!!
        }
    }

    override fun updateBankConnection(bankConnectionId: Long, bankConnection: BankConnection): Long {
        with(bankConnection) {
            val partner = partnerService.createOrGetPartner(partnerId, bankId)
            val currentUser = bankConnectionRepository.getById(bankConnectionId, "bankconnection")
            currentUser.checkWriteAccess()
            //Depending on user status only some values are editable
            val updatedUser = when (currentUser.userStatus) {
                EbicsUserStatusEnum.CREATED, EbicsUserStatusEnum.NEW -> currentUser.updateFromBankConnectionBeforeInitialization(
                    bankConnection,
                    partner
                )
                else -> currentUser.updateFromBankConnectionAfterInitialization(bankConnection)
            }
            bankConnectionRepository.saveAndFlush(updatedUser)
            return bankConnectionId
        }
    }

    private fun BankConnectionEntity.updateFromBankConnectionAfterInitialization(
        bankConnection: BankConnection
    ) = BankConnectionEntity(
        id,
        bankConnection.ebicsVersion,
        userId,
        bankConnection.name,
        dn,
        userStatus,
        useCertificate,
        usePassword,
        partner,
        keyStore,
        creator,
        bankConnection.guestAccess,
        traces,
        properties
    )

    private fun BankConnectionEntity.updateFromBankConnectionBeforeInitialization(
        bankConnection: BankConnection,
        partner: Partner
    ) = BankConnectionEntity(
        id,
        bankConnection.ebicsVersion,
        bankConnection.userId,
        bankConnection.name,
        dn,
        userStatus,
        bankConnection.useCertificate,
        usePassword,
        partner,
        keyStore,
        creator,
        bankConnection.guestAccess,
        traces,
        properties
    )

    override fun deleteBankConnection(bankConnectionId: Long) {
        bankConnectionRepository.getById(bankConnectionId, "bankconnection").checkWriteAccess()
        bankConnectionRepository.deleteById(bankConnectionId)
    }


    /**
     * Resetting user status to default
     * After such reset must be user newly initialized, including creation of user keys
     */
    override fun resetStatus(bankConnectionId: Long): Unit {
        val user = bankConnectionRepository.getById(bankConnectionId, "bankconnection")
        user.checkWriteAccess()
        //Delete user key if available
        user.keyStore?.let { userKeyStoreService.deleteById(it.id!!) }
        //Set user status to CREATED
        user.updateStatus(EbicsUserAction.RESET)
        bankConnectionRepository.saveAndFlush(user)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BankConnectionServiceImpl::class.java)
    }
}