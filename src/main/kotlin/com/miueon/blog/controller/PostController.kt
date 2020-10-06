package com.miueon.blog.controller

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.config.RedisConfig
import com.miueon.blog.exceptions.ApiException
import com.miueon.blog.pojo.post
import com.miueon.blog.service.PostService
import com.miueon.blog.service.impl.RedisServiceImpl
import com.miueon.blog.util.Page4Navigator
import com.miueon.blog.util.UploadUtils

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.net.ConnectException
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api")
class PostController {
    @Autowired
    lateinit var postService: PostService

    @Autowired
    lateinit var redisService: RedisServiceImpl

    private var log = LoggerFactory.getLogger(this.javaClass)

    class postCache

    @GetMapping("/posts/{id}")
    fun getPost(@PathVariable id: Long): ResponseEntity<post> {
        log.debug("REST request to get Post : {}", id)
        // redisService.del(RedisConfig.REDIS_KEY_DATABASE +"post$id")
        var post = redisService[RedisConfig.REDIS_KEY_DATABASE + "post$id"]

        if (post == null) {
            val tmp = postService.findForId(id) ?: throw ApiException("post not exist", HttpStatus.NOT_FOUND)
            redisService[RedisConfig.REDIS_KEY_DATABASE + "post$id"] = tmp
            post = tmp
        }
        return ResponseEntity(post as post, HttpStatus.OK)
    }


    @GetMapping("/posts")
    fun getPostList(@RequestParam(value = "start", defaultValue = "1") start: Int,
                    @RequestParam(value = "size", defaultValue = "5") size: Int): ResponseEntity<Page4Navigator<post>> {

        /* print("test")
         //redisService.del(RedisConfig.REDIS_KEY_DATABASE + "posts${myPage!!.page}.${myPage.size}")
         var posts = redisService[RedisConfig.REDIS_KEY_DATABASE + "posts${myPage!!.page}.${myPage.size}"]
         // todo: how to solve the problem of Unconformity
         if (posts == null) {
             val tmp = postService.findAllByOrderByCreatedDateDescPage(Page<post>(0, 10))
             redisService[RedisConfig.REDIS_KEY_DATABASE + "posts${myPage!!.page}.${myPage.size}"] = tmp
             posts = tmp
         }*/

        val pages = postService.findAllByOrderByCreatedDateDescPage(Page<post>(start.toLong(), size.toLong()),
                5)

        return ResponseEntity(pages, HttpStatus.OK)
    }

    data class keywordDto(val keyword: String)

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
    fun writePost(title:String, md:MultipartFile, request:HttpServletRequest): ResponseEntity<Any?> {
        val id = postService.addPost(title)
        saveMdFile(id, md)
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    private fun saveMdFile(id: Long, mdFile: MultipartFile) {
        val mdFolder = UploadUtils.getMdDirFile()

        try {
            val file = File("${mdFolder.absolutePath}${File.separator}${id}.md")
            mdFile.transferTo(file)

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }




    @PutMapping("/posts/{id}")
    fun editPost(@PathVariable id: Long, title:String, md:MultipartFile): ResponseEntity<Unit> {
        saveMdFile(id, md)
        val post = post()
        post.title = title
        postService.updatePost(post, id)
        redisService.del(RedisConfig.REDIS_KEY_DATABASE + "post$id")
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/posts/{id}")
    fun deletePost(@PathVariable id: Long): ResponseEntity<Unit> {
        postService.findForId(id) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        try {
            redisService.del(RedisConfig.REDIS_KEY_DATABASE + "post$id")
        } catch (e: ConnectException) {
            e.printStackTrace()
        }
        postService.deletePost(id)
        return ResponseEntity(HttpStatus.OK)
    }
}