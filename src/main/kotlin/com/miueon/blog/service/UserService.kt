package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.miueon.blog.mapper.UserMapper
import com.miueon.blog.pojo.user
import com.miueon.blog.pojo.userDto

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService
@Autowired
constructor(var userMapper: UserMapper) {



    /**
     * 保存user登录信息，返回token, only used in refreshing or initiating
     * @param userDto
     */


    // only called in login
    fun getUserInfo(username: String?): userDto? {
        val usrD = userDto()
        val ktQueryWrapper = KtQueryWrapper(user::class.java)
        ktQueryWrapper.eq(user::name, username)
        val usr = userMapper.selectOne(ktQueryWrapper) ?: return null
        usrD.id = usr.id
        usrD.username = usr.name
        usrD.password = usr.password
        return usrD
    }
    fun getRawUser(username: String?): user? {
        val ktQueryWrapper = KtQueryWrapper(user::class.java)
        ktQueryWrapper.eq(user::name, username)
        val usr = userMapper.selectOne(ktQueryWrapper) ?: return null
        return usr
    }


    // called when do jwtAuthentication
    fun getJwtTokenInfo(username: String): userDto? {

        val usrDto = userDto()
        usrDto.username = username
        return usrDto
    }

    fun getUserByUsername(username: String): user {
        val ktQueryWrapper = KtQueryWrapper(user::class.java)
        ktQueryWrapper.eq(user::name, username)
        return userMapper.selectOne(ktQueryWrapper)
    }
}