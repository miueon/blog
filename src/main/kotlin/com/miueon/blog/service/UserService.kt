package com.miueon.blog.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.miueon.blog.mapper.UserMapper
import com.miueon.blog.pojo.user
import com.miueon.blog.pojo.userDto

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService
@Autowired
constructor(var userMapper: UserMapper) :UserDetailsService{

    val passwordEncoder:PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    /**
     * 保存user登录信息，返回token, only used in refreshing or initiating
     * @param userDto
     */
    // @todo: get user info from db
    override fun loadUserByUsername(username: String): UserDetails {
        return User.builder().username("Crux")
                .password(passwordEncoder.encode("123456")).roles("ADMIN", "USER").build()
    }

    fun saveUserLoginInfo(user: UserDetails):String {
        val salt = "123456ef" // todo: replace with Bcrypt.gensalt() and store it in redis
        val algorithm = Algorithm.HMAC256(salt)
        val date = Date(System.currentTimeMillis()+ 3600*1000) // set the expired time at one hour later
        return JWT.create()
                .withSubject(user.username)
                .withExpiresAt(date)
                .withIssuedAt(Date())
                .sign(algorithm)
    }

    // the LginInfo here especially refer to the generated salt
    fun getUserLoginInfo(username: String): UserDetails? {
        val salt = "123456ef"
        val user = loadUserByUsername(username)
        return User.builder().username(user.username)
                .password(salt).authorities(user.authorities).build()
    }
    //delete login info in cache or db
    fun deleteUserLoginInfo(username: String) {

    }




    fun getRawUser(username: String?): user? {
        val ktQueryWrapper = KtQueryWrapper(user::class.java)
        ktQueryWrapper.eq(user::name, username)
        val usr = userMapper.selectOne(ktQueryWrapper) ?: return null
        return usr
    }



    fun getUserByUsername(username: String): user {
        val ktQueryWrapper = KtQueryWrapper(user::class.java)
        ktQueryWrapper.eq(user::name, username)
        return userMapper.selectOne(ktQueryWrapper)
    }
}