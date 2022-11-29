package org.ebics.client.api.bankconnection

import org.ebics.client.api.EbicsUser

/**
 * This may be migrated to EbicsUser
 */
interface BankConnectionEntityInt : EbicsUser {
    val usePassword: Boolean
    val creator: String
    val guestAccess: Boolean
}