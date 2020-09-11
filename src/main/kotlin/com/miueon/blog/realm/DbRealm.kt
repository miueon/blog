package com.miueon.blog.realm

import ch.qos.logback.core.net.ssl.SecureRandomFactoryBean
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.miueon.blog.mapper.UserMapper
import com.miueon.blog.pojo.user
import com.miueon.blog.service.UserService
import org.apache.shiro.authc.*
import org.apache.shiro.authc.credential.HashedCredentialsMatcher
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha256Hash
import org.apache.shiro.crypto.hash.SimpleHash
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.util.ByteSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

class DbRealm(private  var userService: UserService) : AuthorizingRealm() {
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        this.credentialsMatcher = HashedCredentialsMatcher(Sha256Hash.ALGORITHM_NAME)
    }

    override fun supports(token: AuthenticationToken?): Boolean {
        return token is UsernamePasswordToken
    }

    override fun doGetAuthenticationInfo(token: AuthenticationToken?): AuthenticationInfo {
        val usernamePasswordToken = token as UsernamePasswordToken
        val username = usernamePasswordToken.username
        val password = usernamePasswordToken.password
        val usr = userService.getUserInfo(username)
                ?: throw AuthenticationException("user name or password error")
        // the default matcher only take in encrypted password..
        //
        return SimpleAuthenticationInfo(usr, Sha256Hash(password, usr.salt).toHex(),
                ByteSource.Util.bytes(usr.salt), "dbRealm")

    }

    override fun doGetAuthorizationInfo(principals: PrincipalCollection?): AuthorizationInfo {
        return SimpleAuthorizationInfo()
    }
}