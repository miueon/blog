package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.miueon.blog.mapper.UserMapper
import com.miueon.blog.pojo.user
import com.miueon.blog.pojo.userDto
import com.miueon.blog.util.generateSalt
import com.miueon.blog.util.sign
import org.apache.commons.lang3.StringUtils
import org.apache.shiro.crypto.hash.Sha256Hash
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService
@Autowired
constructor(var userMapper: UserMapper) {

    var saltCache: MutableMap<String, String> = HashMap()

    /**
     * 保存user登录信息，返回token, only used in refreshing or initiating
     * @param userDto
     */
    fun generateJwtToken(username: String?): String? {

        val salt = generateSalt()
        saltCache[username!!] = salt!!

        //JwtUtils.generateSalt();
        // todo: after add redis, the salt should be generate randomly
        /**
         * @todo 将salt保存到数据库或者缓存中
         * redisTemplate.opsForValue().set("token:"+username, salt, 3600, TimeUnit.SECONDS);
         */
        return sign(username, saltCache[username], 3600) //生成jwt token，设置过期时间为1小时
    }

    // only called in login
    fun getUserInfo(username: String?): userDto? {
        val usrD = userDto()
        val ktQueryWrapper = KtQueryWrapper(user::class.java)
        ktQueryWrapper.eq(user::name, username)
        val usr = userMapper.selectOne(ktQueryWrapper) ?: return null
        usrD.id = usr.id
        usrD.username = usr.name

        if (StringUtils.isBlank(usr.salt)) {
            usrD.salt = generateSaltAndSaveIt(usr)
        } else {
            usrD.salt = usr.salt
        }

        saltCache[username!!] = usrD.salt!!

        usrD.password = usr.password
        return usrD
    }

    private fun generateSaltAndSaveIt(usr: user): String {
        val salt = generateSalt()
        usr.salt = salt
        usr.password = Sha256Hash(usr.password, salt).toHex()
        val ktUpdateWrapper = KtUpdateWrapper(user::class.java)
        ktUpdateWrapper.eq(user::name, usr.name).set(user::salt, salt).set(user::password, usr.password)
        userMapper.update(usr, ktUpdateWrapper)
        return salt!!
    }

    // called when do jwtAuthentication
    fun getJwtTokenInfo(username: String): userDto? {
        val salt = saltCache[username]
        val usrDto = userDto()
        usrDto.username = username
        usrDto.salt = salt
        return usrDto
    }

    fun getUserByUsername(username: String): user {
        val ktQueryWrapper = KtQueryWrapper(user::class.java)
        ktQueryWrapper.eq(user::name, username)
        return userMapper.selectOne(ktQueryWrapper)
    }
}