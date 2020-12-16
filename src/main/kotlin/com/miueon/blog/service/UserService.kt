package com.miueon.blog.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.miueon.blog.mpg.IdList
import com.miueon.blog.mpg.mapper.AuthorityMapper
import com.miueon.blog.mpg.mapper.UserMapper
import com.miueon.blog.mpg.model.Role
import com.miueon.blog.mpg.model.UserDO

import com.miueon.blog.util.ApiException
import com.miueon.blog.util.RedisKey
import org.slf4j.LoggerFactory


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import java.util.*
import javax.annotation.PostConstruct
import kotlin.collections.HashMap

@Service
class UserService
@Autowired
constructor(var userMapper: UserMapper, var redisService: RedisService
            ) : UserDetailsService {

    val logger = LoggerFactory.getLogger(this.javaClass)
    val passwordEncoder: PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    var userSalt: MutableMap<String, String> = object : MutableMap<String, String> by HashMap() {
        override fun get(key: String): String? {
            return redisService.hGet(RedisKey.USERSALT.name, key) as String
        }

        override fun put(key: String, value: String): String? {
            redisService.hSet(RedisKey.USERSALT.name, key, value, 3600)
            return value
        }
    }

    /**
     * 保存user登录信息，返回token, only used in refreshing or initiating
     * @param userDto
     */
    // @todo: get user info from db
    override fun loadUserByUsername(username: String): UserDetails {
        val ktQueryWrapper = KtQueryWrapper(UserDO::class.java)
        ktQueryWrapper.eq(UserDO::name, username)
        val usr = userMapper.selectOne(ktQueryWrapper)
        usr.role = Role.getByValue(usr.aid!!).name

        return User.builder().username(usr.name)
                .password(usr.password).roles(usr.role).build()
    }

    fun saveUserLoginInfo(user: UserDetails): String {

        val salt = BCrypt.gensalt() // todo: replace with Bcrypt.gensalt() and store it in redis
        userSalt[user.username] = salt
        val algorithm = Algorithm.HMAC256(salt)
        val date = Date(System.currentTimeMillis() + 3600 * 1000) // set the expired time at one hour later
        return JWT.create()
                .withSubject(user.username)
                .withExpiresAt(date)
                .withIssuedAt(Date())
                .sign(algorithm)
    }

    // the LginInfo here especially refer to the generated salt
    fun getUserLoginInfo(username: String): UserDetails? {
        logger.info(" the username: {}", username)
        val salt = userSalt[username]
        logger.info(" the salt is :{}", salt)
        val user = loadUserByUsername(username)
        return User.builder().username(user.username)
                .password(salt as String).authorities(user.authorities).build()
    }

    //delete login info in cache or db
    fun deleteUserLoginInfo(username: String) {
        redisService.del(username)
    }

    fun getRawUser(username: String?): UserDO {
        val ktQueryWrapper = KtQueryWrapper(UserDO::class.java)
        ktQueryWrapper.eq(UserDO::name, username)
        return userMapper.selectOne(ktQueryWrapper)
                ?: throw ApiException("The userName of $username not exist",
                        HttpStatus.BAD_REQUEST)
    }

    @Transactional
    fun addUser(usr: UserDO): UserDO {
        val ktQueryWrapper = KtQueryWrapper(UserDO::class.java)
        ktQueryWrapper.eq(UserDO::name, usr.name)
        if (userMapper.selectCount(ktQueryWrapper) != 0) {
            throw ApiException("the user name for ${usr.name} already exist.",
                    HttpStatus.BAD_REQUEST)
        }
        usr.password = passwordEncoder.encode(usr.password)
        userMapper.insert(usr)
        return usr
    }

    fun selectById(id: Int): UserDO {
        try {
            return userMapper.selectById(id)
        } catch (e: RuntimeException) {
            throw ApiException("User id: $id not exist.", HttpStatus.BAD_REQUEST)
        }
    }

    @Transactional
    fun changePwd(pwd: String) {
        val ktQueryWrapper = KtQueryWrapper(UserDO::class.java)
        ktQueryWrapper.eq(UserDO::aid, 1)
        val admin = userMapper.selectOne(ktQueryWrapper)
        admin.password = passwordEncoder.encode(pwd)
        userMapper.updateById(admin)
    }

}