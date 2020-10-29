package com.miueon.blog.security

import com.auth0.jwt.interfaces.DecodedJWT
import com.miueon.blog.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtRefreshSuccessHandler(private val jwtUserService: UserService):AuthenticationSuccessHandler{
    companion object{
        const val tokenRefreshInterval = 300L // second as unit
    }

    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse,
                                         authentication: Authentication) {
        val jwt:DecodedJWT = (authentication as JwtAuthenticationToken).token
        val shouldRefresh = shouldTokenRefresh(jwt.issuedAt)
        if (shouldRefresh) {
            val newToken = jwtUserService.saveUserLoginInfo(authentication.principal as UserDetails)
            response.setHeader("Authorization", newToken)
        }
    }

    private fun shouldTokenRefresh(issuedAt: Date): Boolean {
        val issuedTime = LocalDateTime.ofInstant(issuedAt.toInstant(), ZoneId.systemDefault())
        return LocalDateTime.now().minusSeconds(tokenRefreshInterval).isAfter(issuedTime)
    }

}