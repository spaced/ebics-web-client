package org.ebics.client.exception

interface EbicsReturnCodeCompanion {

    /**
     * Returns the equivalent `ReturnCode` of a given code
     * @param code the given code
     * @param text the given code text
     * @return the equivalent `ReturnCode`
     */
    fun toReturnCode(code: String, text: String): AbstractEbicsReturnCode

    fun toReturnCode(code: String): AbstractEbicsReturnCode

    fun create(code: String, symbolicName: String): AbstractEbicsReturnCode
}