package org.ebics.client.ebicsrestapi.bankconnection

import org.ebics.client.api.bankconnection.BankConnection
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.BankConnectionService
import org.ebics.client.api.bankconnection.permission.BankConnectionAccessType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("bankconnections")
@CrossOrigin(origins = ["http://localhost:8081"])
class EbicsBankConnectionsResource (
    private val bankConnectionService: BankConnectionService)
{
    @GetMapping("")
    fun listBankConnections(@RequestParam(required = false) permission: BankConnectionAccessType = BankConnectionAccessType.READ): List<BankConnectionEntity> =
        bankConnectionService.findUsers(permission)

    @GetMapping("{userId}")
    fun getBankConnectionById(@PathVariable userId: Long): BankConnectionEntity = bankConnectionService.getUserById(userId)

    @DeleteMapping("{userId}")
    fun deleteBankConnectionById(@PathVariable userId: Long) = bankConnectionService.deleteUser(userId)

    @PostMapping("")
    fun createBankConnection(@RequestBody bankConnection: BankConnection):Long =
        bankConnectionService.createUserAndPartner(bankConnection)

    @PutMapping("{userId}")
    fun updateBankConnection(@PathVariable userId:Long, @RequestBody bankConnection: BankConnection) =
        bankConnectionService.updateUserAndPartner(userId, bankConnection)

    @PostMapping("{userId}/resetStatus")
    fun resetBankConnectionStatus(@PathVariable userId: Long) = bankConnectionService.resetStatus(userId)
}