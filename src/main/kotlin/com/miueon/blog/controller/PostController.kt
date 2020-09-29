package com.miueon.blog.controller

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.fasterxml.jackson.annotation.JsonProperty
import com.miueon.blog.config.RedisConfig
import com.miueon.blog.exceptions.ApiException
import com.miueon.blog.pojo.post
import com.miueon.blog.pojo.user
import com.miueon.blog.pojo.userDto
import com.miueon.blog.service.PostService
import com.miueon.blog.service.impl.RedisServiceImpl
import org.apache.shiro.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource

@RestController
@RequestMapping("/api")
class PostController {
    @Autowired
    lateinit var postService: PostService
    @Autowired
    lateinit var redisService:RedisServiceImpl

    private var log = LoggerFactory.getLogger(this.javaClass)

    class postCache
    @GetMapping("/posts/{id}")
    fun getPost(@PathVariable id: Long): ResponseEntity<post> {
        log.debug("REST request to get Post : {}", id)
       // redisService.del(RedisConfig.REDIS_KEY_DATABASE +"post$id")
        var post = redisService[RedisConfig.REDIS_KEY_DATABASE +"post$id"]

        if (post == null) {
            val tmp =  postService.findForId(id) ?: throw ApiException("post not exist", HttpStatus.NOT_FOUND)
            redisService[RedisConfig.REDIS_KEY_DATABASE + "post$id"] = tmp
            post = tmp
        }
        return ResponseEntity(post as post, HttpStatus.OK)
    }

    data class myPage(val page: Long, val size: Long)

    @GetMapping("/posts")
    fun getPostList(myPage: myPage?): ResponseEntity<List<post>> {
        log.debug("REST request to get Posts : {}", myPage)
       /* print("test")
        //redisService.del(RedisConfig.REDIS_KEY_DATABASE + "posts${myPage!!.page}.${myPage.size}")
        var posts = redisService[RedisConfig.REDIS_KEY_DATABASE + "posts${myPage!!.page}.${myPage.size}"]
        // todo: how to solve the problem of Unconformity
        if (posts == null) {
            val tmp = postService.findAllByOrderByCreatedDateDescPage(Page<post>(0, 10))
            redisService[RedisConfig.REDIS_KEY_DATABASE + "posts${myPage!!.page}.${myPage.size}"] = tmp
            posts = tmp
        }*/
        val posts = postService.findAllByOrderByCreatedDateDescPage(Page<post>(0, 10))

        return ResponseEntity(posts, HttpStatus.OK)
    }

    data class keywordDto(val keyword:String)

    @PostMapping("/search")
    fun search(@RequestBody k: keywordDto): ResponseEntity<List<post>?> {
        log.debug("REST request to search keyword: {}", k.keyword)
        val posts = postService.findByKeyword(k.keyword)
        if (posts != null) {
            return ResponseEntity(posts, HttpStatus.OK)
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    }

    @PostMapping("/posts")
    fun writePost(@RequestBody  postO: post): ResponseEntity<post> {
        val userD = SecurityUtils.getSubject().principal as userDto

        val returnPost = postService.registerPost(postO, userD.username!!)
        return ResponseEntity(returnPost, HttpStatus.CREATED)
    }


    @PutMapping("/posts/{id}")
    fun editPost(@PathVariable id: Long, @RequestBody updatePost: post): ResponseEntity<post> {
        val result = postService.updatePost(updatePost, id)
        redisService.del(RedisConfig.REDIS_KEY_DATABASE +"post$id")
        return ResponseEntity(result, HttpStatus.OK)
    }

    @DeleteMapping("/posts/{id}")
    fun deletePost(@PathVariable id: Long): ResponseEntity<Unit> {
        postService.findForId(id) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        redisService.del(RedisConfig.REDIS_KEY_DATABASE +"post$id")
        postService.deletePost(id)
        return ResponseEntity(HttpStatus.OK)
    }
}