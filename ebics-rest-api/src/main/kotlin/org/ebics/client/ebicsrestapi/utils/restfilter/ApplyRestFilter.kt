package org.ebics.client.ebicsrestapi.utils.restfilter

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class ApplyRestFilter(val filterType: JsonFilterProviderType = JsonFilterProviderType.DefaultNoFilter)
