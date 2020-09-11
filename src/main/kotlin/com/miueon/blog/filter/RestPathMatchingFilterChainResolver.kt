package com.miueon.blog.filter

import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver
import org.apache.shiro.web.util.WebUtils
import org.slf4j.LoggerFactory
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
// this class will filter path like "/user/edit==GET"
class RestPathMatchingFilterChainResolver : PathMatchingFilterChainResolver() {
    @Transient
    private val log = LoggerFactory.getLogger(this::class.java)
    override fun getChain(request: ServletRequest?, response: ServletResponse?,
                          originalChain: FilterChain?): FilterChain? {
        if (!filterChainManager.hasChains()) {
            return null
        }

        val requestURI = getPathWithinApplication(request)
        var isMatch: Boolean
        val iterator: Iterator<String> = filterChainManager.chainNames.iterator()
        var pathPattern: String
        var strings: List<String>? = null
// TODO: add compact pathPattern like [PUT,POST] by regx
        do {
            if (!iterator.hasNext()) return null
            pathPattern = iterator.next()
            strings = pathPattern.split("==")
            log.debug("pathPater split: $strings")
            isMatch = if (strings.size == 2) {
                // compare the http method first
                WebUtils.toHttp(request).method.toUpperCase() ==
                        strings[1].toUpperCase()
            } else {
                false
            }
            // resign pathPattern to compare the uri by pathMatches
            // loop until matches or empty filter chainNames
            pathPattern = strings[0]
            if (pathMatches(pathPattern, requestURI) && strings.size == 1) {
                isMatch = true
            }
        } while (!isMatch)


        if (log.isTraceEnabled) {
            log.trace("Matched path pattern [" + pathPattern +
                    "] for requestURI [" + requestURI + "].  " +
                    "Utilizing corresponding filter chain...");
        }

        if (strings?.size == 2) {
            pathPattern = "$pathPattern==" +
                    WebUtils.toHttp(request).method.toUpperCase()
        }
        log.debug(" now the pathPatter: $pathPattern")
        // return the pathPattern matches the uri

        return if (isMatch) {
            // find the filter definition of pathPattern stored in 'addPathDefinition'
            filterChainManager.proxy(originalChain, pathPattern)
        }else null
    }
}