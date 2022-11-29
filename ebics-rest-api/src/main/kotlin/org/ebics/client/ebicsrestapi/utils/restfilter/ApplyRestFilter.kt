package org.ebics.client.ebicsrestapi.utils.restfilter

/**
 * This annotation is using RestFilterAdvice in order to filter out not needed fields out of REST response
 * It must be used whenever is returned POJO type annotated by JsonFilter annotation
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class ApplyRestFilter(val filterType: JsonFilterProviderType = JsonFilterProviderType.DefaultNoFilter)
