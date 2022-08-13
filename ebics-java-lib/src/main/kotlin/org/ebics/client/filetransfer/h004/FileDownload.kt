/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
package org.ebics.client.filetransfer.h004

import org.ebics.client.api.EbicsSession
import org.ebics.client.api.TransferState
import org.ebics.client.api.trace.TraceManager
import org.ebics.client.api.trace.h004.TraceSession
import org.ebics.client.exception.EbicsException
import org.ebics.client.http.client.TraceableHttpClient
import org.ebics.client.http.factory.HttpTransferSession
import org.ebics.client.http.factory.ITraceableHttpClientFactory
import org.ebics.client.io.ByteArrayContentFactory
import org.ebics.client.io.Joiner
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.h004.*
import org.ebics.client.utils.toHexString
import org.ebics.client.xml.h004.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Handling of file transfers.
 * Files can be transferred to and fetched from the bank.
 * Every transfer may be performed in a recoverable way.
 * For convenience and performance reasons there are also
 * methods that do the whole transfer in one method call.
 * To use the recoverable transfer mode, you may set a working
 * directory for temporarily created files.
 *
 *
 *  EBICS specification 2.4.2 - 6.2 Encryption at application level
 *
 *
 * In the event of an upload transaction, a random symmetrical key is generated in the
 * customer system that is used exclusively within the framework of this transaction both for
 * encryption of the ES’s and for encryption of the order data. This key is encrypted
 * asymmetrically with the financial institution’s public encryption key and is transmitted by the
 * customer system to the bank system during the initialization phase of the transaction.
 *
 *
 * Analogously, in the case of a download transaction a random symmetrical key is generated
 * in the bank system that is used for encryption of the order data that is to be downloaded and
 * for encryption of the bank-technical signature that has been provided by the financial
 * institution. This key is asymmetrically encrypted and is transmitted by the bank system to the
 * customer system during the initialization phase of the transaction. The asymmetrical
 * encryption takes place with the technical subscriber’s public encryption key if the
 * transaction’s EBICS messages are sent by a technical subscriber. Otherwise the
 * asymmetrical encryption takes place with the public encryption key of the non-technical
 * subscriber, i.e. the submitter of the order.
 *
 * @author Hachani
 */

@Component("H004.FileDownload")
class FileDownload(
    private val httpClient: ITraceableHttpClientFactory<TraceableHttpClient>,
    private val traceManager: TraceManager
) {

    /**
     * Fetches a file of the given order type from the bank.
     * You may give an optional start and end date.
     * This type of transfer will run until everything is processed.
     * No transaction recovery is possible.
     * @param downloadOrder type of file to fetch
     * @throws IOException communication error
     * @throws EbicsException server generated error
     */
    @Throws(IOException::class, EbicsException::class)
    fun fetchFile(
        ebicsSession: EbicsSession,
        downloadOrder: EbicsDownloadOrder
    ): ByteArrayOutputStream {
        logger.info(
            String.format(
                "Start downloading file via EBICS sessionId=%s, userId=%s, partnerId=%s, bankURL=%s, order=%s",
                ebicsSession.sessionId,
                ebicsSession.user.userId,
                ebicsSession.user.partner.partnerId,
                ebicsSession.user.partner.bank.bankURL,
                downloadOrder.toString()
            )
        )
        val outputStream = ByteArrayOutputStream()
        val httpSession = HttpTransferSession(ebicsSession)
        val initializer = DownloadInitializationRequestElement(
            ebicsSession,
            downloadOrder.adminOrderType,
            downloadOrder.orderType,
            downloadOrder.startDate,
            downloadOrder.endDate
        )
        initializer.build()
        initializer.validate()
        val traceSession = TraceSession(
            ebicsSession,
            OrderTypeDefinition(downloadOrder.adminOrderType, downloadOrder.orderType),
            false
        )

        val responseBody = httpClient.sendAndTraceRequest(
            httpSession,
            traceSession,
            ByteArrayContentFactory(initializer.prettyPrint())
        )

        val response = DownloadInitializationResponseElement(responseBody)
        traceManager.callAndUpdateLastTrace(traceSession) {
            response.build()
            response.report()
        }

        val state = TransferState(response.segmentsNumber, response.transactionId)
        state.setSegmentNumber(response.segmentNumber)
        val joiner = Joiner(ebicsSession.userCert)
        joiner.append(response.orderData)
        while (state.hasNext()) {
            val segmentNumber: Int = state.next()
            fetchFileSegment(
                ebicsSession,
                downloadOrder.adminOrderType,
                segmentNumber,
                state.isLastSegment,
                state.transactionId,
                joiner,
                traceSession,
                httpSession,
            )
        }
        outputStream.use { dest -> joiner.writeTo(dest, response.transactionKey) }
        val receipt = ReceiptRequestElement(
            ebicsSession,
            state.transactionId
        )
        receipt.build()
        receipt.validate()

        val receiptResponseBody =
            httpClient.sendAndTraceRequest(httpSession, traceSession, ByteArrayContentFactory(receipt.prettyPrint()))

        val receiptResponse = ReceiptResponseElement(receiptResponseBody)
        traceManager.callAndUpdateLastTrace(traceSession) {
            receiptResponse.build()
            receiptResponse.report()
        }
        logger.info(
            String.format(
                "Finished downloading file via EBICS sessionId=%s, userId=%s, partnerId=%s, bankURL=%s, order=%s, transactionId=%s, fileLength=%d",
                ebicsSession.sessionId,
                ebicsSession.user.userId,
                ebicsSession.user.partner.partnerId,
                ebicsSession.user.partner.bank.bankURL,
                downloadOrder.toString(),
                state.transactionId.toHexString(),
                outputStream.size()
            )
        )
        return outputStream
    }

    /**
     * Fetches a given portion of a file.
     * @param segmentNumber the segment number
     * @param lastSegment is it the last segment?
     * @param transactionId the transaction ID
     * @param joiner the portions joiner
     * @throws IOException communication error
     * @throws EbicsException server generated error
     */
    @Throws(IOException::class, EbicsException::class)
    private fun fetchFileSegment(
        session: EbicsSession,
        adminOrderType: EbicsAdminOrderType,
        segmentNumber: Int,
        lastSegment: Boolean,
        transactionId: ByteArray,
        joiner: Joiner,
        traceSession: TraceSession,
        httpSession: HttpTransferSession
    ) {
        val downloader = DownloadTransferRequestElement(
            session,
            adminOrderType,
            segmentNumber,
            lastSegment,
            transactionId
        )
        downloader.build()
        downloader.validate()

        val responseBody =
            httpClient.sendAndTraceRequest(httpSession, traceSession, ByteArrayContentFactory(downloader.prettyPrint()))

        val response = DownloadTransferResponseElement(
            responseBody,
            adminOrderType
        )

        traceManager.callAndUpdateLastTrace(traceSession) {
            response.build()
            response.report()
        }
        joiner.append(response.orderData)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FileDownload::class.java)
    }
}