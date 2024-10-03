package org.ebics.client.ebicsrestapi.key

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class ApiKeyAuthenticationFilter() : OncePerRequestFilter() {

    fun authTokenFromHttpHeader(request: HttpServletRequest): Authentication? {
        val providedAppId = request.getHeader("X-App-Id")
        val providedApiKey = request.getHeader("X-Api-Key")
        if (providedAppId == null || providedApiKey == null) return null
        return ApiKeyAuthenticationToken(providedAppId, providedApiKey)
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        authTokenFromHttpHeader(request)?.let { authentication ->
            SecurityContextHolder.getContext().authentication = authentication
        }
        filterChain.doFilter(request, response)
    }
}
