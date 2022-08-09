package org.ebics.client.exception.h005

import org.ebics.client.exception.EbicsServerException

open class EbicsServerException(ebicsReturnCode: EbicsReturnCode ) : EbicsServerException( ebicsReturnCode )