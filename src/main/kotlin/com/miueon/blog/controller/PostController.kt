package com.miueon.blog.controller

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.aop.CacheTTL
import com.miueon.blog.config.RedisConfig
import com.miueon.blog.exceptions.ApiException
import com.miueon.blog.mpg.model.PostDO
import com.miueon.blog.mpg.IdList
import com.miueon.blog.mpg.IdListDTO
import com.miueon.blog.mpg.PostArchive
import com.miueon.blog.mpg.PostTitle
import com.miueon.blog.service.BulkDelete
import com.miueon.blog.service.DELETEKEY
import com.miueon.blog.service.PostService
import com.miueon.blog.service.TagPostService
import com.miueon.blog.service.impl.RedisServiceImpl

import com.miueon.blog.util.Page4Navigator
import com.miueon.blog.util.Reply
import com.miueon.blog.util.TocSub
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
import java.lang.RuntimeException
import java.net.ConnectException
import java.time.LocalDate
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

    @Autowired
    lateinit var bulkDelete: BulkDelete

    private var log = LoggerFactory.getLogger(this.javaClass)


    private fun getHtmlAndToc(content: String): Pair<String, String> {
        val document = TocSub.PARSER.parse(content)
        return TocSub.RENDERER.render(document) to TocSub.TOC_HTML.get(document)
    }

    @GetMapping("/archive")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getArchiveData(): Reply<List<PostArchive>> {
        return Reply.success(postService.getArchiveData())
    }

    @GetMapping("/latest")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getLatestPostInfo(): Reply<List<PostTitle>> {
        return Reply.success(postService.getLatestPostInfo())
    }

    @GetMapping("/archive/{date}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getPostsByDate(@PathVariable(name = "date") date: String,
                       @RequestParam(name = "start", defaultValue = "1") start: Long,
                       @RequestParam(name = "size", defaultValue = "5") size: Long
    ): Reply<Page4Navigator<PostDO>> {
        val splitResult = date.split("-")
        val year = splitResult[0].toInt()
        val month = splitResult[1].toInt()
        log.info(" year:{}, month:{}", year, month)
        return Reply.success(postService.getPostsByDate(Page(start, size), year, month, 5))
    }

    /**
     * get post by id.
     * @param id
     * @return PostDO with additional info
     */
    @ApiOperation("get post by id in PathVariable")
    @GetMapping("/{id}")
    fun getPost(@PathVariable id: Int,
                @RequestParam(value = "change", defaultValue = "false") change: Boolean?): ResponseEntity<Reply<PostDO>> {
        log.debug("REST request to get Post : {}", id)
        // redisService.del(RedisConfig.REDIS_KEY_DATABASE +"post$id")
//        var post = redisService[RedisConfig.REDIS_KEY_DATABASE + "post$id"]
//
//
//        if (post == null) {
//            val tmp = postService.findForId(id) ?: throw ApiException("post not exist", HttpStatus.NOT_FOUND)
//           // redisService[RedisConfig.REDIS_KEY_DATABASE + "post$id"] = tmp
//            post = tmp
//        }
        val post = postService.findForId(id)

        post.view += 1
        postService.updatePost(post, post.id!!)
        when (change) {
            false -> {
                val (body, toc) = getHtmlAndToc(post.body!!)
                post.body = body
                post.toc = toc
            }
        }
        // todo: replace this with spring-boot schedule & redis. and check whether the guest is admin
        return ResponseEntity(Reply.success(post), HttpStatus.OK)
    }

    @ApiOperation("Pagination for post, the default visible page variation is 5")
    @GetMapping
    fun getPostList(@RequestParam(value = "start", defaultValue = "1")
                    @ApiParam("page number") start: Int,
                    @RequestParam(value = "size", defaultValue = "5")
                    @ApiParam("each page size") size: Int,
                    @RequestParam(value = "cid") cid: Int?

    )
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
        log.info(" cid: {}", cid)
        val pages = postService.findAllByOrderByCreatedDateDescPage(Page(start.toLong(), size.toLong()),
                5, cid)

        return ResponseEntity(Reply.success(pages), HttpStatus.OK)
    }

    @GetMapping("/tag/{tid}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getPostListByTag(@PathVariable("tid") tid: Int): Reply<List<PostDO>> {
        return Reply.success(tagPostService.getPostListByTagId(tid))
    }

    @PostMapping("/tags")
    fun getPostListByTags(@RequestParam(value = "start", defaultValue = "1")
                          @ApiParam("page number") start: Int,
                          @RequestParam(value = "size", defaultValue = "5")
                          @ApiParam("each page size") size: Int,
                          @RequestBody tags: IdList
    )
            : ResponseEntity<Reply<Page4Navigator<PostDO>>> {
        val pids = tagPostService.getPostIdsByTags(tags)
        val pages = postService.findByIdsOrderByCreatedDateDescPage(
                Page(start.toLong(), size.toLong()),
                5, pids)
        return ResponseEntity(Reply.success(pages), HttpStatus.OK)
    }

    @PostMapping("/bulk_delete")
    fun bulkDeletePrep(@RequestBody idList: IdListDTO): Reply<Unit> {
        bulkDelete.prepToDelete(idList.ids, DELETEKEY.POST)
        return Reply.success()
    }

    @GetMapping("/bulk_delete")
    fun bulkDeleteInfo(): Reply<List<PostDO>> {
        try {
            val ids = bulkDelete.getDeleteInfo(DELETEKEY.POST)
            val resultList = postService.findForIds(ids.toList())
            return Reply.success(resultList)
        } catch (e: ApiException) {
            throw e
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/bulk_delete")
    fun bulkDelete(): Reply<Unit> {
        val ids = bulkDelete.getDeleteInfo(DELETEKEY.POST)
        postService.bulkDelete(ids.toList())
        return Reply.success()
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
                       val body: String,
                       val cid: Int?,
                       val tagIdList: List<Int>
    )

    @PostMapping
    fun addPost(@RequestBody dto: PostDTO): Reply<Int> {
        val postDO = PostDO()
        postDO.title = dto.title
        postDO.body = dto.body
        postDO.cid = dto.cid
        postService.savePost(postDO)

        tagPostService.savePostTagRel(postDO.id!!, dto.tagIdList)

        return Reply.success(postDO.id!!)
    }

//    @ApiOperation("add a new post")
//   // @PostMapping
//    fun writePost(title: String, md: MultipartFile): Reply<Unit> {
//        val preSavePost = postService.addPost(title)
//        preSavePost.body = saveMdFile(preSavePost.id!!, md)
//                ?: throw ApiException("Save md file failed")
//        postService.saveBody(preSavePost)
//        return Reply.success()
//    }

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

    //@ApiOperation("update post by id")
    // @PutMapping("/{id}")
    fun editPost(@PathVariable id: Int, title: String, md: MultipartFile): Reply<Unit> {
        saveMdFile(id, md)
        val post = PostDO()
        post.title = title
        postService.updatePost(post, id)
        redisService.del(RedisConfig.REDIS_KEY_DATABASE + "post$id")
        return Reply.success()
    }

    @ApiOperation("update post by id")
    @PutMapping("/{id}")
    @ResponseBody
    fun updatePost(@PathVariable id: Int, @RequestBody dto: PostDTO): Reply<Unit> {
        val post = postService.findForId(id)

        post.title = dto.title
        post.body = dto.body
        post.cid = dto.cid

        postService.updatePost(post, id)
        tagPostService.savePostTagRel(id, dto.tagIdList)
        return Reply.success()
    }

    @ApiOperation("delete post by id")
    @DeleteMapping("/{id}")
    @ResponseBody
    fun deletePost(@PathVariable id: Int): Reply<Unit> {
        postService.findForId(id)
        try {
            redisService.del(RedisConfig.REDIS_KEY_DATABASE + "post$id")
        } catch (e: ConnectException) {
            e.printStackTrace()
        }
        tagPostService.deleteByPid(id)
        postService.deletePost(id)
        return Reply.success()
    }
}