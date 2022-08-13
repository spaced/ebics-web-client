package org.ebics.client.ebicsrestapi.trace

import org.ebics.client.api.trace.BaseTraceEntry
import org.ebics.client.api.trace.TraceService
import org.ebics.client.ebicsrestapi.utils.restfilter.JsonFilterProviderType
import org.ebics.client.ebicsrestapi.utils.restfilter.ApplyRestFilter
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("traces")
@CrossOrigin(origins = ["http://localhost:8081"])
class TraceResource(val traceService: TraceService) {

    @ApplyRestFilter(filterType = JsonFilterProviderType.IdAndNameOnly)
    @GetMapping()
    fun listTraces(): List<BaseTraceEntry> {
        return traceService.findAllTraces()
    }
}