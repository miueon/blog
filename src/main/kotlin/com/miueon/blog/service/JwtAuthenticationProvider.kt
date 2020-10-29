package com.miueon.blog.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.miueon.blog.security.JwtAuthenticationToken
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.www.NonceExpiredException
import java.util.*

class JwtAuthenticationProvider(val userService: UserService):AuthenticationProvider {
    override fun supports(authentication: Class<*>?): Boolean {
        return authentication!!.isAssignableFrom(JwtAuthenticationToken::class.java)
    }

    override fun authenticate(authentication: Authentication?): Authentication {
        val jwt = (authentication as JwtAuthenticationToken).token
        if (jwt.expiresAt.before(Calendar.getInstance().time)) {
            throw NonceExpiredException("Token expires")
        }
        val username = jwt.subject
        val user = userService.getUserLoginInfo(username)
        if (user == null || user.password == null) {
            throw NonceExpiredException("Token expires")
        }
        val encryptSalt = user.password
        try {
            val algorithm = Algorithm.HMAC256(encryptSalt)
            val verifier = JWT.require(algorithm)
                    .withSubject(username)
                    .build()
            verifier.verify(jwt.token)
        } catch (e: Exception) {
            throw BadCredentialsException("JWT token verify fail", e)
        }
        // after verified the name and password(encryptSalt), add some info
        // the other filter will add the authentication msg to SecurityContext
        val token = JwtAuthenticationToken(user, jwt, user.authorities)
        return token
    }
}