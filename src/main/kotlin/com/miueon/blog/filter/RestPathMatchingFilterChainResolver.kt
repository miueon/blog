package com.miueon.blog.filter

import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver
import org.apache.shiro.web.util.WebUtils
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestMethod
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
// this class will filter path like "/user/edit==GET"
class RestPathMatchingFilterChainResolver : PathMatchingFilterChainResolver() {
    @Transient
    private val log = LoggerFactory.getLogger(this::class.java)
    private val methodSet = setOf(RequestMethod.PUT.name,
            RequestMethod.POST.name, RequestMethod.DELETE.name, RequestMethod.OPTIONS.name)
    override fun getChain(request: ServletRequest?, response: ServletResponse?,
                          originalChain: FilterChain?): FilterChain? {
        if (!filterChainManager.hasChains()) {
            return null
        }

        val requestURI = getPathWithinApplication(request)
        var isMatch = false

        var pathPattern = ""


        for (path in filterChainManager.chainNames) {
            if (pathMatches(path, requestURI)) {
                if (methodSet.contains(WebUtils.toHttp(request).method.toUpperCase())) {
                    isMatch = true
                    pathPattern = path
                }
            }
        }



        if (log.isTraceEnabled and isMatch) {
            log.trace("Matched path pattern [" + pathPattern +
                    "] for requestURI [" + requestURI + "].  " +
                    "Utilizing corresponding filter chain...");
        }


        // return the pathPattern matches the uri

        return if (isMatch) {
            // find the filter definition of pathPattern stored in 'addPathDefinition'
            filterChainManager.proxy(originalChain, pathPattern)
        }else null
    }
}