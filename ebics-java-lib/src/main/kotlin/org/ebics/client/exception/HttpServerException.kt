package org.ebics.client.exception

class HttpServerException(
    override val errorCode: String,
    override val errorCodeText: String, message: String?
) : EbicsException(message), IErrorCodeText