package org.ebics.client.exception

import org.ebics.client.exception.h005.EbicsReturnCode
import java.util.*

abstract class AbstractEbicsReturnCode(val code: String, val text: String) {

    /**
     * Tells if the return code is an OK one.
     * @return True if the return code is OK one.
     */
    abstract val isOk: Boolean

    /**
     * Throws an equivalent `EbicsServerException`
     * @throws EbicsServerException
     */
    @Throws(EbicsServerException::class)
    fun throwException() {
        throw EbicsServerException(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as EbicsReturnCode
        return code == that.code
    }

    override fun hashCode(): Int {
        return Objects.hash(code)
    }

    override fun toString(): String {
        return "$code $text"
    }
}