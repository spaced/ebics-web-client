package org.ebics.client.utils

import org.apache.xml.security.c14n.Canonicalizer
import org.apache.xml.security.utils.IgnoreAllErrorHandler
import org.apache.xmlbeans.XmlObject
import org.apache.xpath.XPathAPI
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.exception.EbicsException
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.traversal.NodeIterator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.nio.charset.Charset
import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


/**
 * Convert HEX string to byte array
 */
fun String.decodeHexToByteArray(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

/**
 * Convert byte array to HEX string
 */
fun ByteArray.toHexString(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

fun String.addSpaces(afterCharCount: Int): String = this.replace("(.{$afterCharCount})".toRegex(), "$1 ")

fun LocalDate.toDate(): Date {
    // Getting system timezone
    val systemTimeZone = ZoneId.systemDefault()

    // converting LocalDateTime to ZonedDateTime with the system timezone
    val zonedDateTime = this.atStartOfDay(systemTimeZone)

    // converting ZonedDateTime to Date using Date.from() and ZonedDateTime.toInstant()
    return Date.from(zonedDateTime.toInstant())
}

fun XmlObject.equalXml(xmlObject: XmlObject): Boolean = this.xmlText().equals(xmlObject.xmlText())


inline fun requireNotNullAndNotBlank(
    value: String?,
    lazyMessage: () -> Any = { "The string must not be blank or null" }
): String {
    val notNullValue = requireNotNull(value, lazyMessage)
    require(value.isNotBlank(), lazyMessage)
    return notNullValue
}

fun requireNotNullAndNotBlank(value: String?, stringName: String): String =
    requireNotNullAndNotBlank(value) { "The $stringName must not be null or blank" }

fun <T> requireNotNull(value: T?, paramName: String): T = requireNotNull(value) { "The $paramName must not be null" }

fun ByteArray.toStringSafe(
    charset: Charset = Charsets.UTF_8,
    nonUTF8default: String = "This is probably binary string for given encoding ${charset.displayName()}"
): String {
    val str = String(this, charset)
    val isUtf8 = str.toByteArray(charset).contentEquals(this)
    return if (isUtf8) str else nonUTF8default
}

object Utils {
    /**
     * Compresses an input of byte array
     *
     *
     * The Decompression is ensured via Universal compression
     * algorithm (RFC 1950, RFC 1951) As specified in the EBICS
     * specification (16 Appendix: Standards and references)
     *
     * @param unzippedInput the input to be compressed
     * @return the compressed input data
     * @throws IOException compression failed
     */
    @JvmStatic
    @Throws(EbicsException::class)
    fun zip(unzippedInput: ByteArray): ByteArray {
        try {
            val out = ByteArrayOutputStream(unzippedInput.size)
            DeflaterOutputStream(out).use { output ->
                output.write(unzippedInput)
            }
            return out.toByteArray()
        } catch (e: IOException) {
            throw EbicsException(e.message)
        }
    }

    /**
     * Generates a random nonce.
     *
     *
     * EBICS Specification 2.4.2 - 11.6 Generation of the transaction IDs:
     *
     *
     * Transaction IDs are cryptographically-strong random numbers with a length of 128 bits. This
     * means that the likelihood of any two bank systems using the same transaction ID at the
     * same time is sufficiently small.
     *
     *
     * Transaction IDs are generated by cryptographic pseudo-random number generators (PRNG)
     * that have been initialized with a real random number (seed). The entropy of the seed should
     * be at least 100 bits.
     *
     * @return a random nonce.
     * @throws EbicsException nonce generation fails.
     */
    @JvmStatic
    @Throws(EbicsException::class)
    fun generateNonce(): ByteArray {
        val secureRandom: SecureRandom
        return try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG")
            secureRandom.generateSeed(16)
        } catch (e: NoSuchAlgorithmException) {
            throw EbicsException(e.message)
        }
    }

    /**
     * Uncompresses a given byte array input.
     *
     *
     * The Decompression is ensured via Universal compression
     * algorithm (RFC 1950, RFC 1951) As specified in the EBICS
     * specification (16 Appendix: Standards and references)
     *
     * @param zippedInput the zipped input.
     * @return the uncompressed data.
     */
    @Throws(EbicsException::class)
    fun unzip(zippedInput: ByteArray): ByteArray {
        try {
            //Assumption for initial buffer size
            //compression ratio would be more than 50%
            val out = ByteArrayOutputStream(zippedInput.size * 2)
            InflaterOutputStream(out).use { output ->
                output.write(zippedInput)
            }
            return out.toByteArray()
        } catch (e: IOException) {
            throw EbicsException(e.message)
        }
    }

    /**
     * Canonizes an input with inclusive c14n without comments algorithm.
     *
     *
     * EBICS Specification 2.4.2 - 5.5.1.1.1 EBICS messages in transaction initialization:
     *
     *
     * The identification and authentication signature includes all XML elements of the
     * EBICS request whose attribute value for @authenticate is equal to “true”. The
     * definition of the XML schema “ebics_request.xsd“ guarantees that the value of the
     * attribute @authenticate is equal to “true” for precisely those elements that also
     * need to be signed.
     *
     *
     * Thus, All the Elements with the attribute authenticate = true and their
     * sub elements are considered for the canonization process. This is performed
     * via the [selectNodeIterator(Node, String)][XPathAPI.selectNodeIterator].
     *
     * @param input the byte array XML input.
     * @return the canonized form of the given XML
     * @throws EbicsException
     */
    @JvmStatic
    @Throws(EbicsException::class)
    fun canonize(input: ByteArray): ByteArray {
        val factory: DocumentBuilderFactory
        val builder: DocumentBuilder
        val document: Document
        val iter: NodeIterator
        val output: ByteArrayOutputStream
        var node: Node?
        return try {
            factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            factory.isValidating = true
            builder = factory.newDocumentBuilder()
            builder.setErrorHandler(IgnoreAllErrorHandler())
            document = builder.parse(ByteArrayInputStream(input))
            iter = XPathAPI.selectNodeIterator(document, "//*[@authenticate='true']")
            output = ByteArrayOutputStream()
            while (iter.nextNode().also { node = it } != null) {
                val canonicalizer: Canonicalizer = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS)
                output.write(canonicalizer.canonicalizeSubtree(node))
            }
            output.toByteArray()
        } catch (e: Exception) {
            throw EbicsException(e.message)
        }
    }


}