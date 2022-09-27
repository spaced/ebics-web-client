package org.ebics.client.api.bank.versions

import org.ebics.client.api.bank.Bank

interface VersionSupportService {
    fun updateVersionSupport(versionSupport: VersionSupport)
    fun updateVersionSupport(base: VersionSupportBase, bank: Bank)
}