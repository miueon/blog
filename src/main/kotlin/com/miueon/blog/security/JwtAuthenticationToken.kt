package com.miueon.blog.security

import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class JwtAuthenticationToken:AbstractAuthenticationToken {
    companion object{
        const val serialVersionUID = 3981518947978158945L
    }
    private  var principal:UserDetails? = null
    private  var credentials:String? = null
    val token:DecodedJWT



    constructor(token: DecodedJWT):super(emptyList()){
        this.token = token
    }
    constructor(principal: UserDetails, token: DecodedJWT,
                authorities: Collection<GrantedAuthority>):super(authorities){
        this.principal = principal
        this.token = token
    }


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


}