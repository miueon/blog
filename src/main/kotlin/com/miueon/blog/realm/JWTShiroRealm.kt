package com.miueon.blog.realm

import com.miueon.blog.pojo.JWTToken
import com.miueon.blog.service.UserService
import com.miueon.blog.util.getUsername
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.slf4j.LoggerFactory

class JWTShiroRealm(private  var userService: UserService) : AuthorizingRealm() {
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        this.credentialsMatcher = JWTCredentialsMatcher()
    }

    override fun supports(token: AuthenticationToken?): Boolean {
        return token is JWTToken
    }

    override fun doGetAuthenticationInfo(token: AuthenticationToken?): AuthenticationInfo {
        val jwtToken = token as JWTToken
        val deCodedToken = jwtToken.token

        val usr = userService.getJwtTokenInfo(getUsername(deCodedToken)!!)
                ?: throw AuthenticationException("token expired, login again")
        val authenticationInfo = SimpleAuthenticationInfo(usr, usr.salt, "jwtRealm")
        return authenticationInfo

    }

    override fun doGetAuthorizationInfo(principals: PrincipalCollection?): AuthorizationInfo {
        return SimpleAuthorizationInfo()
    }


}