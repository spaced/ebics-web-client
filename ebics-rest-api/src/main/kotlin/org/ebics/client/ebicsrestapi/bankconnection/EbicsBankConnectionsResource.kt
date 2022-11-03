package org.ebics.client.ebicsrestapi.bankconnection

import org.ebics.client.api.bankconnection.BankConnection
import org.ebics.client.api.bankconnection.BankConnectionService
import org.ebics.client.api.bankconnection.permission.BankConnectionAccessType
import org.ebics.client.api.healthstatus.BankConnectionWithHealthStatus
import org.ebics.client.api.healthstatus.HealthStatusEnrichmentService
import org.ebics.client.ebicsrestapi.utils.restfilter.ApplyRestFilter
import org.ebics.client.ebicsrestapi.utils.restfilter.JsonFilterProviderType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("bankconnections")
@CrossOrigin(origins = ["http://localhost:8081"])
class EbicsBankConnectionsResource (
    private val bankConnectionService: BankConnectionService,
    private val healthStatusEnrichmentService: HealthStatusEnrichmentService,
)
{
    @GetMapping("")
    @ApplyRestFilter(filterType = JsonFilterProviderType.DefaultNoFilter)
    fun listBankConnections(@RequestParam(required = false) permission: BankConnectionAccessType = BankConnectionAccessType.READ): List<BankConnectionWithHealthStatus> {
        val bankConnections = bankConnectionService.findBankConnections(permission)
        return healthStatusEnrichmentService.enrichBankConnectionsWithStatus(bankConnections, null)
    }


    @GetMapping("{userId}")
    @ApplyRestFilter(filterType = JsonFilterProviderType.DefaultNoFilter)
    fun getBankConnectionById(@PathVariable userId: Long): BankConnectionWithHealthStatus {
        val bankConnection = bankConnectionService.getBankConnectionById(userId)
        return healthStatusEnrichmentService.enrichBankConnectionWithStatus(bankConnection, null)
    }

    @DeleteMapping("{userId}")
    fun deleteBankConnectionById(@PathVariable userId: Long) = bankConnectionService.deleteBankConnection(userId)

    @PostMapping("")
    fun createBankConnection(@RequestBody bankConnection: BankConnection):Long =
        bankConnectionService.createBankConnection(bankConnection)

    @PutMapping("{userId}")
    fun updateBankConnection(@PathVariable userId:Long, @RequestBody bankConnection: BankConnection) =
        bankConnectionService.updateBankConnection(userId, bankConnection)

    @PostMapping("{userId}/resetStatus")
    fun resetBankConnectionStatus(@PathVariable userId: Long) = bankConnectionService.resetStatus(userId)
}