package org.ebics.client.api.healthstatus

import org.ebics.client.exception.EbicsException
import java.time.LocalDateTime

interface ConnectionStatus {
    val status: ConnectionStatusDetail
    val lastError: EbicsException?
    val lastOkTimestamp: LocalDateTime?
}