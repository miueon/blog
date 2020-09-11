package com.miueon.blog.realm

import com.auth0.jwt.JWT

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.miueon.blog.pojo.userDto
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.credential.CredentialsMatcher
import org.slf4j.LoggerFactory
import java.io.UnsupportedEncodingException

class JWTCredentialsMatcher : CredentialsMatcher {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun doCredentialsMatch(Atoken: AuthenticationToken?, info: AuthenticationInfo?): Boolean {
        /*
         token – the AuthenticationToken created by filter createToken.
            so this  type is self defined JWTToken

         info – the AuthenticationInfo stored in the system. return from jwtRealm doGetAuthenticationInfo
         */
        val token = Atoken?.credentials as String

        val stored = info?.credentials
        val salt = stored as String
        val usrDto = info.principals.primaryPrincipal as userDto

        try {
            val alg = Algorithm.HMAC256(salt)
            val verifier = JWT.require(alg)
                    .withClaim("username", usrDto.username)
                    .build()
            verifier.verify(token)
            return true
        } catch (e: UnsupportedEncodingException) {
            log.error("Token error: {}", e.message)
        } catch (e: JWTVerificationException) {
            log.error("Token error: {}", e.message)
        }
        return false
    }
}