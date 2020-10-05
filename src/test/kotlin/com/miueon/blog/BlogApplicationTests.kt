package com.miueon.blog

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mapper.UserMapper
import com.miueon.blog.pojo.post
import com.miueon.blog.service.CommentService
import com.miueon.blog.service.PostService

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BlogApplicationTests
@Autowired constructor(
        val userMapper: UserMapper,
        val postService: PostService,
        val commentService: CommentService
) {


    @Test
    fun testMapper() {
        var users = userMapper.selectAll()
        users.forEach { println(it.toString()) }
    }



    @Test
    fun testGetComments() {
        var usr = userMapper.selectById(1)
        var comments = commentService.findCommentsByPostId(1)
        println(comments?.get(0)?.content)
    }

}
