package org.ebics.client.exception

open class EbicsServerException(val ebicsReturnCode: AbstractEbicsReturnCode) : EbicsException(message = ebicsReturnCode.toString()), IErrorCodeText {
    override val errorCode: String
        get() = ebicsReturnCode.code
    override val errorCodeText: String
        get() = ebicsReturnCode.text
}