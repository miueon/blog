package com.miueon.blog.util

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.miueon.blog.mpg.PostELDO
import com.miueon.blog.mpg.mapper.PostMapper
import com.miueon.blog.mpg.mapper.UserMapper
import com.miueon.blog.mpg.model.PostDO
import com.miueon.blog.mpg.model.UserDO
import com.miueon.blog.service.PostEService
import com.miueon.blog.service.PostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CheckAdminAccountExist : CommandLineRunner {
    @Autowired
    lateinit var userMapper: UserMapper
    @Autowired
    lateinit var postEService: PostEService
    @Autowired
    lateinit var postMapper:PostMapper
    val passwordEncoder: PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
    override fun run(vararg args: String?) {
        val ktQueryWrapper = KtQueryWrapper(UserDO::class.java)
        ktQueryWrapper.eq(UserDO::name, "miueon")
        val result = userMapper.selectCount(ktQueryWrapper)
        if (result == 0) {
            val ktUpdateWrapper = KtUpdateWrapper(UserDO::class.java)
            val userDO = UserDO()
            userDO.name = "miueon"
            userDO.aid = 1
            userDO.email = "crux@gmail.com"
            userDO.url = "miueon.xyz"
            userDO.password = passwordEncoder.encode("123456")
            userMapper.insert(userDO)
        }
        val posts = postMapper.selectList(KtQueryWrapper(PostDO::class.java).orderByDesc(PostDO::createdDate))
        postEService.init(posts.map { PostELDO.fromDO(it) })

    }
}