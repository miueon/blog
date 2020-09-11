package com.miueon.blog.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import java.io.UnsupportedEncodingException
import java.util.*

/**
 * 获得token中的信息无需secret解密也能获得
 * @return token中包含的签发时间
 */
fun getIssuedAt(token: String?): Date? {
    return try {
        val jwt = JWT.decode(token)
        jwt.issuedAt
    } catch (e: JWTDecodeException) {
        null
    }
}

/**
 * 获得token中的信息无需secret解密也能获得
 * @return token中包含的用户名
 */
fun getUsername(token: String?): String? {
    return try {
        val jwt = JWT.decode(token)
        jwt.getClaim("username").asString()
    } catch (e: JWTDecodeException) {
        null
    }
}

/**
 * 生成签名,expireTime后过期
 * @param username 用户名
 * @param time 过期时间s
 * @return 加密的token
 */
fun sign(username: String?, salt: String?, time: Long): String? {
    return try {
        val date = Date(System.currentTimeMillis() + time * 1000)
        val algorithm = Algorithm.HMAC256(salt)
        // 附带username信息
        JWT.create()
                .withClaim("username", username)
                .withExpiresAt(date)
                .withIssuedAt(Date())
                .sign(algorithm)
    } catch (e: UnsupportedEncodingException) {
        null
    }
}

/**
 * token是否过期, not need salt
 * @return true：过期
 */
fun isTokenExpired(token: String?): Boolean {
    val now = Calendar.getInstance().time
    val jwt = JWT.decode(token)
    return jwt.expiresAt.before(now)
}

/**
 * 生成随机盐,长度32位
 * @return
 */
fun generateSalt(): String? {
    val secureRandom = SecureRandomNumberGenerator()
    return secureRandom.nextBytes(16).toHex()
}