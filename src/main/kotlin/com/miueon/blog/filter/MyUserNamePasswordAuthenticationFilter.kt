package com.miueon.blog.filter

import com.alibaba.fastjson.JSON
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.util.Assert
import org.springframework.util.StreamUtils
import org.springframework.util.StringUtils
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MyUserNamePasswordAuthenticationFilter :
        AbstractAuthenticationProcessingFilter(
                // filter post request for url "/login"
                AntPathRequestMatcher("/login", "POST")) {
    override fun afterPropertiesSet() {
        Assert.notNull(authenticationManager, "Authentication manager must be specified")
        Assert.notNull(successHandler, "Success handler must be specified")
        Assert.notNull(failureHandler, "Falure handler must be specified")
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        // in filter, the body can't be automatic parse, you need to do it manually
        val body = StreamUtils.copyToString(request.inputStream, StandardCharsets.UTF_8)
        var username:String? = null
        var password:String? = null
        if (StringUtils.hasText(body)) {
            val jsonObj = JSON.parseObject(body)
            username = jsonObj.getString("username")
            password = jsonObj.getString("password")
        }
        username = username?: ""
        password = password?: ""
        username = username.trim()
        val authRequest =  UsernamePasswordAuthenticationToken(username, password)
        return this.authenticationManager.authenticate(authRequest)
    }
}