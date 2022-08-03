package org.ebics.client.ebicsrestapi.utils.restfilter

import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.lang.Nullable
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice


@ControllerAdvice
class RestFilterAdvice(
    private val jsonFilter: JsonFilterProviderHelper
) : ResponseBodyAdvice<Any> {

    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return returnType.hasMethodAnnotation(ApplyRestFilter::class.java)
                || returnType.containingClass.isAnnotationPresent(ApplyRestFilter::class.java)
    }

    override fun beforeBodyWrite(
        @Nullable body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        val restFilterAnnotation = if (returnType.hasMethodAnnotation(ApplyRestFilter::class.java))
            returnType.getMethodAnnotation(ApplyRestFilter::class.java)
        else
            returnType.containingClass.getAnnotation(ApplyRestFilter::class.java)

        val filterType =
            requireNotNull(restFilterAnnotation) { "Missing required annotation, should be covered by supports() method properly" }.filterType
        when(filterType) {
            JsonFilterProviderType.DefaultNoFilter -> log.info("Applying Default REST filter to response $filterType, all fields are returned")
            else -> log.info("Applying REST filter to response $filterType")
        }
        return jsonFilter.applyJsonFilter(body!!, filterType)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ResponseBodyAdvice::class.java)
    }
}