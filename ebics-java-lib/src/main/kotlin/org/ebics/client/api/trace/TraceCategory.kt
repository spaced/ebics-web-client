package org.ebics.client.api.trace

import org.ebics.client.exception.EbicsServerException
import org.ebics.client.exception.HttpServerException
import java.lang.Exception

enum class TraceCategory {
    Request,
    HttpResponseOk,
    HttpResponseError,
    EbicsResponseOk,
    EbicsResponseError,
    GeneralError;
    companion object {
        fun fromException(exception: Exception): TraceCategory {
            return when {
                EbicsServerException::class.java.isAssignableFrom(exception.javaClass) -> EbicsResponseError
                HttpServerException::class.java.isAssignableFrom(exception.javaClass) -> HttpResponseError
                else -> GeneralError
            }
        }
    }
}