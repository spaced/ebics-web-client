package org.ebics.client.api.trace

import org.ebics.client.exception.EbicsServerException
import org.ebics.client.exception.HttpServerException

enum class TraceCategory {
    Request,
    RequestError,
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