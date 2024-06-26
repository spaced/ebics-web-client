package org.ebics.client.ebicsrestapi.bankconnection.h005

import org.ebics.client.ebicsrestapi.bankconnection.GetOrderTypesRequest
import org.ebics.client.ebicsrestapi.bankconnection.UploadResponse
import org.ebics.client.ebicsrestapi.bankconnection.UserIdPass
import org.ebics.client.ebicsrestapi.bankconnection.UserPass
import org.ebics.client.order.h005.OrderType
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController("EbicsAPIResourceH005")
@RequestMapping("bankconnections/{userId}/H005")
@CrossOrigin(origins = ["http://localhost:8081"])
class EbicsAPIResource(
    private val ebicsKeyManagementAPI: EbicsKeyManagementAPI,
    private val ebicsFileTransferAPI: EbicsFileTransferAPI
) {
    @PostMapping("sendINI")
    fun sendINI(@PathVariable userId: Long, @RequestBody userPass: UserPass) =
        ebicsKeyManagementAPI.sendINI(UserIdPass(userId, userPass.password))

    @PostMapping("sendHIA")
    fun sendHIA(@PathVariable userId: Long, @RequestBody userPass: UserPass) =
        ebicsKeyManagementAPI.sendHIA(UserIdPass(userId, userPass.password))

    @PostMapping("sendHPB")
    fun sendHPB(@PathVariable userId: Long, @RequestBody userPass: UserPass) =
        ebicsKeyManagementAPI.sendHPB(UserIdPass(userId, userPass.password))

    @PostMapping("upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @PathVariable userId: Long,
        @RequestPart uploadRequest: UploadRequest,
        @RequestPart uploadFile: MultipartFile
    ): UploadResponse = ebicsFileTransferAPI.uploadFile(userId, uploadRequest, uploadFile)

    @PostMapping("download")
    fun downloadFile(
        @PathVariable userId: Long,
        @RequestBody downloadRequest: DownloadRequest
    ): ResponseEntity<Resource> = ebicsFileTransferAPI.downloadFile(userId, downloadRequest)

    @PostMapping("orderTypes")
    fun getOrderTypes(@PathVariable userId: Long, @RequestBody orderTypesRequest: GetOrderTypesRequest): List<OrderType> =
        ebicsFileTransferAPI.getOrderTypes(userId, orderTypesRequest.password, orderTypesRequest.useCache)
}