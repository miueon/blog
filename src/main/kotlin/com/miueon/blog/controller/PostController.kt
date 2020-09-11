package com.miueon.blog.controller

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.exceptions.ApiException
import com.miueon.blog.pojo.post
import com.miueon.blog.service.PostService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class PostController(@Autowired val postService: PostService
) {
    private var log = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/posts/{id}")
    fun getPost(@PathVariable id: Int): ResponseEntity<post> {
        log.debug("REST request to get Post : {}", id)
        val post = postService.findForId(id) ?: throw ApiException("post not exist", HttpStatus.NOT_FOUND)
        return ResponseEntity(post, HttpStatus.OK)
    }

    data class myPage(val page: Long, val size: Long)

    @GetMapping("/posts")
    fun getPostList(myPage: myPage?): ResponseEntity<List<post>> {
        log.debug("REST request to get Posts : {}", myPage)
        val posts = postService.findAllByOrderByCreatedDateDescPage(Page<post>(0, 10))
        return ResponseEntity(posts, HttpStatus.OK)
    }

    // TODO: post mapping

    // TODO: puMapping

    // TODO: deleteMapping
}