package com.miueon.blog.filter

import com.miueon.blog.pojo.JWTToken
import com.miueon.blog.pojo.userDto
import com.miueon.blog.service.UserService
import com.miueon.blog.util.getIssuedAt
import com.miueon.blog.util.isTokenExpired
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpStatus
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.subject.Subject
import org.apache.shiro.web.filter.PathMatchingFilter
import org.apache.shiro.web.filter.authc.AuthenticatingFilter
import org.apache.shiro.web.util.WebUtils
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestMethod
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

const val tokenRefreshInterval = 300
const val AuthHeader = "Authorization"

class JwtAuthFilter(private  var userService: UserService) : AuthenticatingFilter() {
    private val log = LoggerFactory.getLogger(JwtAuthFilter::class.java)

    //private var methods: MutableSet<String>? = null



    init {
        this.loginUrl = "/auth/login"
    }

    //    false to continue checking, true to controller
    override fun preHandle(request: ServletRequest?, response: ServletResponse?): Boolean {
       log.trace("Trace: in preHandle, ${WebUtils.toHttp(request).requestURI}")

        return super.preHandle(request, response)

    }

    override fun postHandle(request: ServletRequest?, response: ServletResponse?) {
        this.fillCorsHeader(WebUtils.toHttp(request), WebUtils.toHttp(response))
        // in front end
        request?.setAttribute("jwtShiroFilter.FILTERED", true)
    }

    override fun isAccessAllowed(request: ServletRequest?, response: ServletResponse?, mappedValue: Any?)
            : Boolean {
        log.trace("Trace: in access")
        if (this.isLoginRequest(request, response)) return true
        // check the request by given loginUrl in constructor

        var afterFiltered = false
        if (request?.getAttribute("jwtShiroFilter.FILTERED") != null) {
            afterFiltered = true
        }
        if (afterFiltered) {
            return true
        }

        var allowed = false
        try {
            log.trace("trace for uri: ${WebUtils.toHttp(request).requestURL}")
            allowed = executeLogin(request, response)
            // this method in super class will first createToken() (override below↓),
            // then call the Subject.login()
        } catch (e: IllegalStateException) {
            log.error("Not found any token")
        } catch (e: Exception) {
            log.error("Error occurs when login", e)
        }

        return allowed || super.isPermissive(mappedValue)
    }

    override fun createToken(request: ServletRequest?, response: ServletResponse?): AuthenticationToken? {
        val jwtToken = getAuthzHeader(request)
        // do not use 'and' in if. it is not shorted
        if (StringUtils.isNotBlank(jwtToken)) {
            if (!isTokenExpired(jwtToken)) {
                return JWTToken(jwtToken)
            }
        }
        return null
    }

    override fun onAccessDenied(request: ServletRequest?, response: ServletResponse?): Boolean {
        val httpResponse = WebUtils.toHttp(response)
        httpResponse.characterEncoding = "UTF-8"
        httpResponse.contentType = "application/json;charset=UTF-8"
        httpResponse.status = HttpStatus.SC_NOT_FOUND
        fillCorsHeader(WebUtils.toHttp(request), httpResponse)
        return false
    }

    override fun onLoginSuccess(token: AuthenticationToken?, subject: Subject?, request: ServletRequest?,
                                response: ServletResponse?): Boolean {
        val httpResponse = WebUtils.toHttp(response)
        var newToken: String? = null
        if (token is JWTToken) {
            val user = subject?.principal as userDto
            val shouldRefresh = getIssuedAt(token.token)?.let { shouldTokenRefresh(it) } ?: false
            if (shouldRefresh) {
                newToken = userService.generateJwtToken(user.username)
            }
        }
        if (StringUtils.isNotBlank(newToken)) {
            httpResponse.setHeader(AuthHeader, newToken)
        }
        return true
    }

    // onLoginFailure will direct to onAccessDenied
    override fun onLoginFailure(token: AuthenticationToken?, e: AuthenticationException?,
                                request: ServletRequest?, response: ServletResponse?): Boolean {
        log.error("Validate token fail, token:{}, error:{}", token.toString(), e!!.message)

        return false
    }

    // Utils
    private fun fillCorsHeader(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"))
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,HEAD")
        httpServletResponse.setHeader("Access-Control-Allow-Headers",
                httpServletRequest.getHeader("Access-Control-Request-Headers"))
    }

    private fun getAuthzHeader(request: ServletRequest?): String {
        val httpRequest = WebUtils.toHttp(request)
        val header = httpRequest.getHeader("Authorization")
        return StringUtils.removeStart(header, "Bearer ")
    }

    private fun shouldTokenRefresh(issueAt: Date): Boolean {
        val issueTime = LocalDateTime.ofInstant(issueAt.toInstant(), ZoneId.systemDefault())
        // 在tokenRefreshInterval时间之后的token才刷新
        return LocalDateTime.now().minusSeconds(tokenRefreshInterval.toLong()).isAfter(issueTime)
    }
}