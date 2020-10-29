package com.miueon.blog.security

import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpStatusLoginFailureHandler :AuthenticationFailureHandler{
    override fun onAuthenticationFailure(request: HttpServletRequest,
                                         response: HttpServletResponse,
                                         exception: AuthenticationException) {
        response.status = HttpStatus.UNAUTHORIZED.value()
    }
}