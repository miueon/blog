package com.miueon.blog.security

import com.miueon.blog.filter.JwtAuthenticationFilter
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutFilter

class JwtLoginConfigurer : AbstractHttpConfigurer<JwtLoginConfigurer, HttpSecurity>() {
    val authFilter = JwtAuthenticationFilter()
    override fun configure(builder: HttpSecurity) {
        authFilter.authenticationManager = builder.getSharedObject(AuthenticationManager::class.java)
        authFilter.failureHandler = HttpStatusLoginFailureHandler()

        val filter = postProcess(authFilter)
        builder.addFilterBefore(filter, LogoutFilter::class.java)
    }

    fun permissiveRequestUrls(vararg urls: String):JwtLoginConfigurer {
        authFilter.setPermissiveUrl(*urls)
        return this
    }

    fun tokenValidSuccessHandler(successHandler: AuthenticationSuccessHandler):JwtLoginConfigurer {
        authFilter.successHandler = successHandler
        return this
    }
}