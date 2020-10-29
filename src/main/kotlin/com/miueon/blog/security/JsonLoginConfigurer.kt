package com.miueon.blog.security

import com.miueon.blog.filter.MyUserNamePasswordAuthenticationFilter
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy

class JsonLoginConfigurer
    : AbstractHttpConfigurer<JsonLoginConfigurer, HttpSecurity>() {
    val authFilter: MyUserNamePasswordAuthenticationFilter = MyUserNamePasswordAuthenticationFilter()
    override fun configure(builder:HttpSecurity) {
        authFilter.setAuthenticationManager(builder?.getSharedObject(AuthenticationManager::class.java))
        authFilter.setAuthenticationFailureHandler(HttpStatusLoginFailureHandler())
        // no to set the context to session
        authFilter.setSessionAuthenticationStrategy(NullAuthenticatedSessionStrategy())

        val filter:MyUserNamePasswordAuthenticationFilter = postProcess(authFilter)
        builder?.addFilterAfter(filter, LogoutFilter::class.java)
    }

    fun loginSuccessHandler(authSuccessHandler: AuthenticationSuccessHandler):
            JsonLoginConfigurer{
        authFilter.setAuthenticationSuccessHandler(authSuccessHandler)
        return this
    }
}