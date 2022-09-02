package org.ebics.client.ebicsrestapi.exceptions

import org.ebics.client.exception.IErrorCodeText
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class ServerErrorMessage(
    timestamp: LocalDateTime = LocalDateTime.now(),
    httpStatus: HttpStatus,
    status: Int = httpStatus.value(),
    error: String = httpStatus.reasonPhrase,
    message: String,
    causeMessage: String?,
    exceptionClass: String,
    override val errorCode: String,
    override val errorCodeText: String
) : ErrorMessage(timestamp, httpStatus, status, error, message, causeMessage, exceptionClass), IErrorCodeText