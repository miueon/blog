package com.miueon.blog.filter

import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OptionRequestFilter:OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if (request.method == "OPTIONS") {
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,HEAD")
            response.setHeader("Access-Control-Allow-Headers", response.getHeader("Access-Control-Request-Headers"))
            return
        }
        filterChain.doFilter(request, response)
    }
}