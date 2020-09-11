package com.miueon.blog.pojo

import org.apache.shiro.authc.HostAuthenticationToken

class JWTToken:HostAuthenticationToken {
    companion object{
        private val serialVersionUID = 9217639903967592166L
    }

    public var token:String
    private  var host:String?
    override fun getHost(): String? {
        return host
    }

    constructor(token: String):this(token,null)
    constructor(token: String, host: String?){
        this.host = host
        this.token = token
    }

    override fun getPrincipal(): Any {
        return token
    }

    override fun getCredentials(): Any {
        return token
    }

    override fun toString(): String {
        return "$token:$host"
    }
}