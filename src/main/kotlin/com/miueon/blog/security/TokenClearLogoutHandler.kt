package com.miueon.blog.security

import com.miueon.blog.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.logout.LogoutHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TokenClearLogoutHandler(private val jwtUserService: UserService):LogoutHandler {
    override fun logout(p0: HttpServletRequest?, p1: HttpServletResponse?, p2: Authentication?) {
        clearToken(p2)
    }
    private fun clearToken(authentication: Authentication?) {
        if (authentication == null) {
            return
        }
        val user:UserDetails? = authentication.principal as UserDetails
        if (user != null && user.username != null) {
            jwtUserService.deleteUserLoginInfo(user.username)
        }
    }
}