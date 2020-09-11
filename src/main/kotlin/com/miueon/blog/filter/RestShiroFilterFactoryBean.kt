package com.miueon.blog.filter

import org.apache.shiro.spring.web.ShiroFilterFactoryBean
import org.apache.shiro.web.filter.mgt.FilterChainManager
import org.apache.shiro.web.filter.mgt.FilterChainResolver
import org.apache.shiro.web.mgt.WebSecurityManager
import org.apache.shiro.web.servlet.AbstractShiroFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanInitializationException
import java.lang.IllegalArgumentException

class RestShiroFilterFactoryBean : ShiroFilterFactoryBean() {
    private var log = LoggerFactory.getLogger(this::class.java)
    override fun createInstance(): AbstractShiroFilter {
        log.debug("Creating Shiro Filter instance.")
        val msg: String
        if (securityManager == null) {
            msg = "SecurityManager property must be set."
            throw BeanInitializationException(msg)
        } else if (securityManager !is WebSecurityManager) {
            msg = "The security manager does not implement " +
                    "the WebSecurityManager interface."
            throw BeanInitializationException(msg)
        } else {
            val manager = createFilterChainManager() as FilterChainManager
            // RestPathMatchingFilterChainResolver
            val chainResolver = RestPathMatchingFilterChainResolver()
            chainResolver.filterChainManager = manager
            return SpringShiroFilter(securityManager as WebSecurityManager,
                    chainResolver)
        }
    }

    private class SpringShiroFilter(webSecurityManager: WebSecurityManager,
                                    resolver: FilterChainResolver) :
            AbstractShiroFilter() {
        init {
            webSecurityManager?.let {
                securityManager = webSecurityManager
                resolver?.let { filterChainResolver = resolver }
            } ?: throw IllegalArgumentException("WebSecurityManager property cannot be null.")
        }
    }
}