package com.miueon.blog.controller

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.config.RedisConfig
import com.miueon.blog.exceptions.ApiException
import com.miueon.blog.mpg.model.PostDO
import com.miueon.blog.pojo.post
import com.miueon.blog.service.PostService
import com.miueon.blog.service.TagPostService
import com.miueon.blog.service.impl.RedisServiceImpl
import com.miueon.blog.util.Page4Navigator
import com.miueon.blog.util.Reply
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.ConnectException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@Api(tags = ["PostController"], description = "Post crud API")
@RequestMapping("/api/post")
class PostController {
    @Autowired
    lateinit var postService: PostService
    @Autowired
    lateinit var tagPostService: TagPostService

    @Autowired
    lateinit var redisService: RedisServiceImpl

    private var log = LoggerFactory.getLogger(this.javaClass)

    class postCache

    /**
     * get post by id.
     * @param id
     * @return PostDO with additional info
      */
    @ApiOperation("get post by id in PathVariable")
    @GetMapping("/{id}")
    fun getPost(@PathVariable id: Int): ResponseEntity<Reply<PostDO>> {
        log.debug("REST request to get Post : {}", id)
        // redisService.del(RedisConfig.REDIS_KEY_DATABASE +"post$id")
//        var post = redisService[RedisConfig.REDIS_KEY_DATABASE + "post$id"]
//        // todo
//
//        if (post == null) {
//            val tmp = postService.findForId(id) ?: throw ApiException("post not exist", HttpStatus.NOT_FOUND)
//           // redisService[RedisConfig.REDIS_KEY_DATABASE + "post$id"] = tmp
//            post = tmp
//        }
        val post = postService.findForId(id) ?: throw ApiException("post not exist", HttpStatus.NOT_FOUND)
        return ResponseEntity(Reply.success(post), HttpStatus.OK)
    }

    @ApiOperation("Pagination for post, the default visible page variation is 5")
    @GetMapping
    fun getPostList(@RequestParam(value = "start", defaultValue = "1")
                    @ApiParam("page number") start: Int,
                    @RequestParam(value = "size", defaultValue = "5")
                    @ApiParam("each page size") size: Int)
            : ResponseEntity<Reply<Page4Navigator<PostDO>>> {

        /* print("test")
         //redisService.del(RedisConfig.REDIS_KEY_DATABASE + "posts${myPage!!.page}.${myPage.size}")
         var posts = redisService[RedisConfig.REDIS_KEY_DATABASE + "posts${myPage!!.page}.${myPage.size}"]
         // todo: how to solve the problem of Unconformity
         if (posts == null) {
             val tmp = postService.findAllByOrderByCreatedDateDescPage(Page<post>(0, 10))
             redisService[RedisConfig.REDIS_KEY_DATABASE + "posts${myPage!!.page}.${myPage.size}"] = tmp
             posts = tmp
         }*/

        val pages = postService.findAllByOrderByCreatedDateDescPage(Page(start.toLong(), size.toLong()),
                5)

        return ResponseEntity(Reply.success(pages), HttpStatus.OK)
    }

    data class keywordDto(val keyword: String)

//    @PostMapping("/search")
//    fun search(@RequestBody k: keywordDto): ResponseEntity<List<PostDTO>?> {
//        log.debug("REST request to search keyword: {}", k.keyword)
//        val posts = postService.findByKeyword(k.keyword)
//        if (posts != null) {
//            return ResponseEntity(posts, HttpStatus.OK)
//        }
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
//
//    }
    data class PostDTO(val title: String,
                       val body:String,
                       val cid:Int?,
                       val tagIdList:List<Int>
                       )
    @PostMapping
    fun addPost(@RequestBody dto: PostDTO):Reply<Int> {
        val postDO = PostDO()
        postDO.title = dto.title
        postDO.body = dto.body
        postDO.cid = dto.cid
        postService.savePost(postDO)

        tagPostService.savePostTagRel(postDO.id!!, dto.tagIdList)

        return Reply.success(postDO.id!!)
    }

    @ApiOperation("add a new post")
   // @PostMapping
    fun writePost(title: String, md: MultipartFile): Reply<Unit> {
        val preSavePost = postService.addPost(title)
        preSavePost.body = saveMdFile(preSavePost.id!!, md)
                ?: throw ApiException("Save md file failed")
        postService.saveBody(preSavePost)
        return Reply.success()
    }

    private fun saveMdFile(id: Int, mdFile: MultipartFile): String? {
        var body: String? = null
        try {
            val file = File("${postService.downloadMdPath}${File.separator}${id}.md")
            mdFile.transferTo(file)
            body = postService.readBodyFromMdFile(id)
            // todo: after save as md file, read it, and store it in database
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return body
    }
    @ApiOperation("download markdown file.")
    @GetMapping("/download")
    fun downloadMdFile(@RequestParam("pid") pid: Int, response: HttpServletResponse): String {
        val file = File("${postService.downloadMdPath}${File.separator}${pid}.md")
        if (!file.exists()) {
            return "download file not exits"
        }
        val post = postService.findForId(pid)
                ?: return "request of $pid isn't in database"
        response.reset()
        response.contentType = "application/octet-stream"
        response.characterEncoding = "utf-8"
        response.setContentLength(file.length().toInt())
        response.setHeader("Content-Disposition", "attachment;filename=${post.title}.md")
        try {
            BufferedInputStream(FileInputStream(file)).use {
                val buff = ByteArray(1024)
                val os = response.outputStream
                var i = 0
                i = it.read(buff)
                while (i != -1) {
                    os.write(buff, 0, i)
                    os.flush()
                    i = it.read(buff)
                }
            }
        } catch (e: IOException) {
            log.error("{}", e)
            return "download failed"
        }
        return "download success"
    }

    @ApiOperation("update post by id")
    @PutMapping("/{id}")
    fun editPost(@PathVariable id: Int, title: String, md: MultipartFile): Reply<Unit> {
        saveMdFile(id, md)
        val post = PostDO()
        post.title = title
        postService.updatePost(post, id)
        redisService.del(RedisConfig.REDIS_KEY_DATABASE + "post$id")
        return Reply.success()
    }

    @ApiOperation("delete post by id")
    @DeleteMapping("/{id}")
    fun deletePost(@PathVariable id: Int):Reply<Unit> {
        postService.findForId(id)
        try {
            redisService.del(RedisConfig.REDIS_KEY_DATABASE + "post$id")
        } catch (e: ConnectException) {
            e.printStackTrace()
        }
        postService.deletePost(id)
        return Reply.success()
    }
}