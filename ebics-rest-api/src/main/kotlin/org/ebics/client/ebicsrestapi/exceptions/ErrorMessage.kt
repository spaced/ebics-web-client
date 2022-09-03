package org.ebics.client.ebicsrestapi.exceptions

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

open class ErrorMessage(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    @JsonIgnore
    val httpStatus: HttpStatus,
    val httpStatusCode: Int = httpStatus.value(),
    val httpStatusReasonPhrase: String = httpStatus.reasonPhrase,
    val message: String,
    val causeMessage: String?,
    val exceptionClass: String,
)
