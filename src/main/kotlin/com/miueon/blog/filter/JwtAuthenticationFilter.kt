package com.miueon.blog.filter

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.miueon.blog.security.JwtAuthenticationToken
import org.apache.commons.lang3.StringUtils
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.util.Assert

import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(
        val requiresAuthenticationRequestMatcher: RequestMatcher =
                RequestHeaderRequestMatcher("Authorization")
): OncePerRequestFilter() {
    var successHandler:AuthenticationSuccessHandler = SavedRequestAwareAuthenticationSuccessHandler()
        set(value) {
            Assert.notNull(field, "successHandler cannot be null")
            field = value
        }

    var failureHandler:AuthenticationFailureHandler = SimpleUrlAuthenticationFailureHandler()
        set(value) {
            Assert.notNull(field, "failureHandler cannot be null")
            field = value
        }
    private var permissiveRequestMatchers:MutableList<RequestMatcher>? = null
    lateinit var authenticationManager: AuthenticationManager


    override fun afterPropertiesSet() {
        Assert.notNull(authenticationManager, "authenticationManager must be specified")
        Assert.notNull(successHandler, "AuthenticationSuccessHandler must be specified")
        Assert.notNull(failureHandler, "AuthenticationFailureHandler must be specified")
    }
    private fun requiresAuthentication(request: HttpServletRequest, response: HttpServletResponse): Boolean {
        return requiresAuthenticationRequestMatcher.matches(request)
    }

    private fun getJwtToken(request: HttpServletRequest): String {
        val authInfo = request.getHeader("Authorization")
        return StringUtils.removeStart(authInfo, "Bearer ")
    }
    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        //header没带token的，直接放过，因为部分url匿名用户也可以访问
        //如果需要不支持匿名用户的请求没带token，这里放过也没问题，
        // 因为SecurityContext中没有认证信息，后面会被权限控制模块拦截
        if (!requiresAuthentication(request, response)) {
            filterChain.doFilter(request, response)
            return
        }

        var authResult:Authentication? = null
        var failed:AuthenticationException? = null
        try {
            val token: String = getJwtToken(request)

            logger.info(token)

            if (StringUtils.isNotBlank(token)) {
                val authToken = JwtAuthenticationToken(JWT.decode(token))
                authResult = this.authenticationManager.authenticate(authToken)
            } else {
                failed = InsufficientAuthenticationException("JWT is empty")
            }
        } catch (e: JWTDecodeException) {
            logger.error("JWT format error", e)
            failed = InsufficientAuthenticationException("JWT format error", e)
        } catch (e: InternalAuthenticationServiceException) {
            logger.error("An internal error occurred while trying to authenticate the user.", e)
            failed = e
        } catch (e: AuthenticationException) {
            failed = e
        }

        if (authResult != null) {
            successfulAuthentication(request, response, filterChain, authResult)
            logger.info("JWT Success!")
        } else if (!permissiveRequest(request)) {
            unsuccessfulAuthentication(request, response, failed!!)
            logger.info("JWT Failed!")
            return
        }

        filterChain.doFilter(request, response)
    }

    fun unsuccessfulAuthentication(request: HttpServletRequest, response: HttpServletResponse,
                                   failed: AuthenticationException) {
        SecurityContextHolder.clearContext()
        failureHandler.onAuthenticationFailure(request, response, failed)
    }

    fun successfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain,
                                 authResult: Authentication) {
        SecurityContextHolder.getContext().authentication = authResult
        successHandler.onAuthenticationSuccess(request, response, authResult)
    }


    fun permissiveRequest(request: HttpServletRequest): Boolean {
        if (permissiveRequestMatchers == null) {
            return false
        }
        permissiveRequestMatchers?.forEach { if (it.matches(request)) return true }
        return false
    }

    fun setPermissiveUrl(vararg urls: String) {
        if (permissiveRequestMatchers == null) {
            permissiveRequestMatchers = ArrayList()
        }
        urls.forEach {
            permissiveRequestMatchers?.add(AntPathRequestMatcher(it))
        }
    }
}