package org.ebics.client.api.trace

import org.ebics.client.exception.EbicsException
import java.lang.Exception

enum class TraceCategory {
    httpOk,
    httpError,
    ebicsOk,
    ebicsError;
    companion object {
        fun fromException(exception: Exception): TraceCategory {
            return when {
                exception.javaClass.isAssignableFrom(EbicsException::class.java) -> ebicsError
                else -> httpError
            }
        }
    }
}