package org.ebics.client.api.bank.versions

import org.ebics.client.api.bank.Bank
import org.springframework.stereotype.Service

@Service
class VersionSupportServiceImpl(private val versionSupportRepository: VersionSupportRepository) : VersionSupportService {
    override fun updateVersionSupport(versionSupport: VersionSupport) {
        versionSupportRepository.save(versionSupport)
    }

    override fun updateVersionSupport(base: VersionSupportBase, bank: Bank) {
        updateVersionSupport(VersionSupport.fromBaseAndBank(base, bank))
    }
}