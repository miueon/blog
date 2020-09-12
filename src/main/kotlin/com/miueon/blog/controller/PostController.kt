package com.miueon.blog.controller

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.exceptions.ApiException
import com.miueon.blog.pojo.post
import com.miueon.blog.pojo.user
import com.miueon.blog.pojo.userDto
import com.miueon.blog.service.PostService
import org.apache.shiro.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class PostController(@Autowired val postService: PostService
) {
    private var log = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/posts/{id}")
    fun getPost(@PathVariable id: Long): ResponseEntity<post> {
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

    @PostMapping("/posts")
    fun writePost(@RequestBody postO: post):ResponseEntity<post>{
        val userD = SecurityUtils.getSubject().principal as userDto

        val returnPost = postService.registerPost(postO, userD.username!!)
        return ResponseEntity(returnPost, HttpStatus.CREATED)
    }


    @PutMapping("/posts/{id}")
    fun editPost(@PathVariable id: Long, @RequestBody updatePost: post): ResponseEntity<post> {
        val result = postService.updatePost(updatePost,id)
        return ResponseEntity(result, HttpStatus.OK)
    }

    @DeleteMapping("/posts/{id}")
    fun deletePost(@PathVariable id: Long):ResponseEntity<Unit> {
        postService.findForId(id) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        postService.deletePost(id)
        return ResponseEntity(HttpStatus.OK)
    }
}