package org.ebics.client.ebicsrestapi.partner

import org.ebics.client.api.partner.Partner
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.ebicsrestapi.utils.restfilter.ApplyRestFilter
import org.ebics.client.ebicsrestapi.utils.restfilter.JsonFilterProviderHelper
import org.ebics.client.ebicsrestapi.utils.restfilter.JsonFilterProviderType
import org.springframework.http.converter.json.MappingJacksonValue
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("partners")
@CrossOrigin(origins = ["http://localhost:8081"])
class EbicsPartnersResource (
    private val partnerService: PartnerService)
{
    @GetMapping("")
    @ApplyRestFilter(filterType = JsonFilterProviderType.DefaultNoFilter)
    fun listPartners(): List<Partner> = partnerService.findAll()

    @GetMapping("{id}")
    @ApplyRestFilter(filterType = JsonFilterProviderType.DefaultNoFilter)
    fun getPartnerById(@PathVariable partnerId: Long): Partner = partnerService.getPartnerById(partnerId)

    @DeleteMapping("{id}")
    fun deletePartnerById(@PathVariable partnerId: Long) = partnerService.deletePartnerById(partnerId)

    @PostMapping("")
    fun createPartner(@RequestParam ebicsPartnerId: String, @RequestParam bankId: Long): Partner =
        partnerService.createOrGetPartner(ebicsPartnerId, bankId)
}