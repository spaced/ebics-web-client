package org.ebics.client.ebicsrestapi.bankconnection.h005

import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.trace.BankConnectionTraceSession
import org.ebics.client.api.trace.IFileService
import org.ebics.client.api.trace.orderType.EbicsService
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.ebicsrestapi.bankconnection.UploadResponse
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.bankconnection.session.IEbicsSessionFactory
import org.ebics.client.ebicsrestapi.utils.IFileDownloadCache
import org.ebics.client.filetransfer.h005.FileDownload
import org.ebics.client.filetransfer.h005.FileUpload
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.EbicsAdminOrderType
import org.ebics.client.order.h005.EbicsDownloadOrder
import org.ebics.client.order.h005.EbicsUploadOrder
import org.ebics.client.order.h005.OrderType
import org.ebics.client.utils.toDate
import org.ebics.client.xml.h005.HTDResponseOrderDataElement
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component("EbicsFileAPIH005")
class EbicsFileTransferAPI(
    private val sessionFactory: IEbicsSessionFactory,
    private val fileDownloadCache: IFileDownloadCache,
    private val fileService: IFileService,
    private val fileUpload: FileUpload,
    private val fileDownload: FileDownload,
) {

    fun uploadFile(userId: Long, uploadRequest: UploadRequest, uploadFile: MultipartFile): UploadResponse {
        val session = sessionFactory.getSession(UserIdPass(userId, uploadRequest.password))
        val order = EbicsUploadOrder(
            uploadRequest.orderService,
            uploadRequest.signatureFlag,
            uploadRequest.edsFlag,
            uploadRequest.fileName,
            uploadRequest.params ?: emptyMap()
        )
        val orderType = OrderTypeDefinition(EbicsAdminOrderType.BTU, EbicsService.fromEbicsService(uploadRequest.orderService))
        val traceSession = BankConnectionTraceSession(session, orderType)
        val response = fileUpload.sendFile(session, traceSession, uploadFile.bytes, order)
        fileService.addUploadedFile(traceSession, uploadFile.bytes)
        return UploadResponse(response.orderNumber, response.transactionId)
    }

    fun downloadFile(userId: Long, downloadRequest: DownloadRequest): ResponseEntity<Resource> {
        val session = sessionFactory.getSession(UserIdPass(userId, downloadRequest.password))

        val order = EbicsDownloadOrder(
            downloadRequest.adminOrderType,
            downloadRequest.orderService,
            downloadRequest.startDate?.toDate(),
            downloadRequest.endDate?.toDate(),
            downloadRequest.params
        )
        val orderType = OrderTypeDefinition(EbicsAdminOrderType.BTD, downloadRequest.orderService?.let { EbicsService.fromEbicsService(downloadRequest.orderService) } )
        val traceSession = BankConnectionTraceSession(session, orderType, false)
        val outputStream = fileDownload.fetchFile(session, traceSession, order)
        fileService.addDownloadedFile(traceSession, outputStream.toByteArray())
        val resource = ByteArrayResource(outputStream.toByteArray())
        return ResponseEntity.ok().contentLength(resource.contentLength()).body(resource)
    }

    fun getOrderTypes(userId: Long, password: String, useCache: Boolean): List<OrderType> {
        val session = sessionFactory.getSession(UserIdPass(userId, password))
        val orderType = OrderTypeDefinition(EbicsAdminOrderType.HTD)
        val traceSession = BankConnectionTraceSession(session, orderType, false)
        val htdFileContent = fileDownloadCache.getLastFileCached(
            session,
            traceSession,
            OrderTypeDefinition(EbicsAdminOrderType.HTD),
            EbicsVersion.H005,
            useCache
        )

        return HTDResponseOrderDataElement.getOrderTypes(htdFileContent)
    }
}