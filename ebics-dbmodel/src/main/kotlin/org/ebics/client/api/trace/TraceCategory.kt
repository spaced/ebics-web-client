package org.ebics.client.api.trace

import org.ebics.client.exception.EbicsServerException
import org.ebics.client.exception.HttpServerException
import java.lang.Exception

enum class TraceCategory {
    httpOk,
    httpServerError,
    ebicsOk,
    ebicsServerError,
    generalError;
    companion object {
        fun fromException(exception: Exception): TraceCategory {
            return when {
                EbicsServerException::class.java.isAssignableFrom(exception.javaClass) -> ebicsServerError
                HttpServerException::class.java.isAssignableFrom(exception.javaClass) -> httpServerError
                else -> generalError
            }
        }
    }
}