package org.ebics.client.ebicsrestapi.exceptions

import org.ebics.client.api.AlreadyExistException
import org.ebics.client.api.FunctionException
import org.ebics.client.api.NotFoundException
import org.ebics.client.exception.EbicsException
import org.ebics.client.exception.HttpServerException
import org.ebics.client.exception.IErrorCodeText
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionAdvice {
    @ExceptionHandler(
        value = [NotFoundException::class, AlreadyExistException::class, FunctionException::class,
            IllegalAccessException::class, AccessDeniedException::class, Exception::class]
    )
    fun exceptionHandler(ex: Exception): ResponseEntity<ErrorMessage> {
        when (ex) {
            is org.ebics.client.exception.h004.NoDownloadDataAvailableException -> logger.info(ex.message)
            is org.ebics.client.exception.h005.NoDownloadDataAvailableException -> logger.info(ex.message)
            else -> logger.error("Reporting occurred exception occur", ex)
        }

        val status: HttpStatus = when (ex) {
            is NotFoundException -> HttpStatus.NOT_FOUND
            is AlreadyExistException -> HttpStatus.CONFLICT //EXISTING RESOURCE
            is FunctionException -> HttpStatus.INTERNAL_SERVER_ERROR
            is org.ebics.client.exception.h004.NoDownloadDataAvailableException -> HttpStatus.SERVICE_UNAVAILABLE
            is org.ebics.client.exception.h005.NoDownloadDataAvailableException -> HttpStatus.SERVICE_UNAVAILABLE
            is org.ebics.client.exception.h004.EbicsServerException -> HttpStatus.INTERNAL_SERVER_ERROR
            is org.ebics.client.exception.h005.EbicsServerException -> HttpStatus.INTERNAL_SERVER_ERROR
            is HttpServerException -> HttpStatus.INTERNAL_SERVER_ERROR
            is EbicsException -> HttpStatus.INTERNAL_SERVER_ERROR
            is IllegalAccessException -> HttpStatus.FORBIDDEN
            is AccessDeniedException -> HttpStatus.FORBIDDEN
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        val exceptionMessage = ex.message ?: ex.toString()
        val exceptionCauseMessage = ex.cause?.message
        val exceptionClass = ex.javaClass.canonicalName
        when (ex) {
            is IErrorCodeText -> return ResponseEntity<ErrorMessage>(
                ServerErrorMessage(
                    httpStatus = status,
                    message = exceptionMessage,
                    causeMessage = exceptionCauseMessage,
                    exceptionClass = exceptionClass,
                    errorCode = ex.errorCode,
                    errorCodeText = ex.errorCodeText,
                ), status
            )
            else ->  return ResponseEntity<ErrorMessage>(
                ErrorMessage(
                    httpStatus = status,
                    message = exceptionMessage,
                    causeMessage = exceptionCauseMessage,
                    exceptionClass = exceptionClass,
                ), status
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExceptionAdvice::class.java)
    }
}