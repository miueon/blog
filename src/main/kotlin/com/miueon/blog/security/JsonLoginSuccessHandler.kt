package com.miueon.blog.security

import com.miueon.blog.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonLoginSuccessHandler(val jwtUserService: UserService):AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(request: HttpServletRequest,
                                         response: HttpServletResponse,
                                         authentication: Authentication) {
        val token = jwtUserService.saveUserLoginInfo(authentication.principal as UserDetails)
        response.setHeader("Authorization", token)

    }
}