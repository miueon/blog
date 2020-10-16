package com.miueon.blog.filter

import org.apache.commons.lang3.StringUtils
import org.springframework.data.util.StreamUtils
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.util.Assert
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class JwtAuthenticationFilter(
        private var requiresAuthenticationRequestMatcher:RequestMatcher
): OncePerRequestFilter() {
     var successHandler: AuthenticationSuccessHandler = SavedRequestAwareAuthenticationSuccessHandler()

     var failureHandler:AuthenticationFailureHandler = SimpleUrlAuthenticationFailureHandler()
    private  var permissiveRequestMatchers:MutableList<RequestMatcher>? = null
     lateinit var authenticationManager:AuthenticationManager



    override fun afterPropertiesSet() {
        Assert.notNull(authenticationManager, "authenticationManager must be specified")
        Assert.notNull(successHandler, "AuthenticationSuccessHandler must be specified")
        Assert.notNull(failureHandler, "AuthenticationFailureHandler must be specified")
    }

    protected fun getJwtToken(request: HttpServletRequest):String {
        val authInfo = request.getHeader("Authorization")
        return StringUtils.removeStart(authInfo, "Bearer ")
    }
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        //header没带token的，直接放过，因为部分url匿名用户也可以访问
        //如果需要不支持匿名用户的请求没带token，这里放过也没问题，
        // 因为SecurityContext中没有认证信息，后面会被权限控制模块拦截
        if (!requiresAuthentication(request, response)) {
            filterChain.doFilter(request,response)
            return
        }
        var authResult:Authentication? = null
        var failed:AuthenticationException? = null

        try{
            val token = getJwtToken(request)
            if (StringUtils.isNotBlank(token)) {
                val authToken = JwtAuthenticationToken()
            }
        }
    }
    protected fun unsuccessfulAuthentication(request: HttpServletRequest,
                                             response: HttpServletResponse,
                                             failed: AuthenticationException) {
        SecurityContextHolder.clearContext()
        failureHandler.onAuthenticationFailure(request, response, failed)
    }
    protected fun successfulAuthentication(request: HttpServletRequest,
                                           response: HttpServletResponse,
                                           chain: FilterChain,
                                           authResult: Authentication) {
        SecurityContextHolder.getContext().authentication = authResult
        successHandler.onAuthenticationSuccess(request, response, authResult)
    }

    protected fun requiresAuthentication(request: HttpServletRequest, response: HttpServletResponse):Boolean {
        return requiresAuthenticationRequestMatcher.matches(request)
    }

    protected fun permissiveRequest(request: HttpServletRequest): Boolean {
        if (permissiveRequestMatchers == null) {
            return false
        }
        permissiveRequestMatchers!!.forEach{
            if (it.matches(request)) {
                return true
            }
        }
        return false
    }

    fun setPermissiveUrl(vararg urls: String) {
        if (permissiveRequestMatchers == null) {
            permissiveRequestMatchers = ArrayList()
        }
        for (url in urls) {
            permissiveRequestMatchers!!.add(AntPathRequestMatcher(url))
        }
    }


}