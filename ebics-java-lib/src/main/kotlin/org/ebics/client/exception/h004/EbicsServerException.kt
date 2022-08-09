package org.ebics.client.exception.h004

import org.ebics.client.exception.EbicsServerException

open class EbicsServerException(ebicsReturnCode: EbicsReturnCode ) : EbicsServerException( ebicsReturnCode )