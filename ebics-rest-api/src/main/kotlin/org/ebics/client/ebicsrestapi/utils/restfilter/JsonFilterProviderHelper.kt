package org.ebics.client.ebicsrestapi.utils.restfilter

import com.fasterxml.jackson.databind.ser.FilterProvider
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import org.springframework.http.converter.json.MappingJacksonValue
import org.springframework.stereotype.Component

@Component
class JsonFilterProviderHelper {

    /**
     * This is the default filter which is not cutting anything out, so the full JSON is returned
     */
    private val defaultNoFilter: FilterProvider = SimpleFilterProvider().addFilter(
        "bankConnectionPropertiesFilter",
        SimpleBeanPropertyFilter.serializeAllExcept()
    ).addFilter(
        "partnerPropertiesFilter",
        SimpleBeanPropertyFilter.serializeAllExcept()
    ).addFilter(
        "bankPropertiesFilter",
        SimpleBeanPropertyFilter.serializeAllExcept()
    )

    /**
     * This is the filter which returns only the important entity IDs
     */
    private val idAndNameOnlyFilter: FilterProvider = SimpleFilterProvider().addFilter(
        "bankConnectionPropertiesFilter",
        SimpleBeanPropertyFilter.filterOutAllExcept("id", "name", "userId", "partner")
    ).addFilter(
        "partnerPropertiesFilter",
        SimpleBeanPropertyFilter.filterOutAllExcept("id", "partnerId", "bank")
    ).addFilter(
        "bankPropertiesFilter",
        SimpleBeanPropertyFilter.filterOutAllExcept("id", "name", "bankURL")
    )

    private val filterMap = mapOf(
        JsonFilterProviderType.DefaultNoFilter to defaultNoFilter,
        JsonFilterProviderType.IdAndNameOnly to idAndNameOnlyFilter
    )

    fun applyJsonFilter(bean: Any, filterType: JsonFilterProviderType = JsonFilterProviderType.DefaultNoFilter): MappingJacksonValue {
        val filterProvider = requireNotNull(filterMap[filterType]) { "The filter type: $filterType is not defined" }
        return MappingJacksonValue(bean).apply { filters = filterProvider }
    }
}