package com.miueon.blog.pojo

import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class JwtAuthenticationToken:AbstractAuthenticationToken {
    companion object{
        private val serialVersionUID:Long = 3981518947978158945L
    }

    private var principal: UserDetails? = null
    private val credentials: String? = null
    private var token: DecodedJWT? = null

    constructor(token: DecodedJWT):super(Collections.emptyList()){
        this.token = token
    }

    constructor(principal: UserDetails, token: DecodedJWT, authorities: Collection<out GrantedAuthority>)


    override fun setDetails(details: Any?) {
        super.setDetails(details)
        this.isAuthenticated = true
    }

    override fun getCredentials(): Any? {
        return credentials
    }

    override fun getPrincipal(): Any? {
        return principal
    }

    fun getToken():DecodedJWT? {
        return token
    }

}