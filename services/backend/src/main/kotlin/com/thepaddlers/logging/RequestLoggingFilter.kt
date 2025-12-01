package com.thepaddlers.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import java.util.*

@Component
class RequestLoggingFilter : GenericFilterBean() {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val req = request as? HttpServletRequest
        val traceId = req?.getHeader("X-Trace-Id") ?: UUID.randomUUID().toString()
        MDC.put("trace_id", traceId)
        MDC.put("request_id", req?.getHeader("X-Request-Id") ?: UUID.randomUUID().toString())
        // Example: extract user_id/org_id from JWT or session
        MDC.put("user_id", req?.getHeader("X-User-Id") ?: "")
        MDC.put("org_id", req?.getHeader("X-Org-Id") ?: "")
        try {
            chain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}

