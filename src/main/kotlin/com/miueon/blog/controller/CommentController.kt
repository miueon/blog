package com.miueon.blog.controller

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mpg.*
import com.miueon.blog.mpg.model.CommentDO
import com.miueon.blog.service.*
import com.miueon.blog.util.Page4Navigator
import com.miueon.blog.util.Reply
import com.miueon.blog.validator.Insert
import com.miueon.blog.validator.Update
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/api/comment")
class CommentController
@Autowired
constructor(
        val commentService: CommentService,
        val redisService: RedisService,
        val bulkDelete: BulkDelete,
        val userService: UserService
) {
    val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/post")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getCommentsByPid(@RequestParam("pid")
                         @NotBlank(message = "the pid shouldn't be empty") @Min(1) pid: Int)
            : Reply<List<CommentDO>> {
        val result = commentService.getAllCommentOfPost(pid)
        result.map {
            it.uid = null
        }
        return Reply.success(result)
    }

    @GetMapping("/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getCommentById(@PathVariable @NotBlank(message = "the id shouldn't be empty") id: Int): Reply<CommentDTO> {
        return Reply.success(CommentDTO.fromDO(commentService.getById(id)))
    }


    @GetMapping("/post_title")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getPostTitleList(): Reply<List<PostTitle>> {
        return Reply.success(commentService.getPostTitleList())
    }

    @GetMapping("/comment_usrInfo/{name}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getCommentUsrInfoByName(@PathVariable("name") @NotBlank(message = "offer user name to get its info")
                                usrName: String): Reply<CommentUserInfo> {
        val rawInfo = userService.getRawUser(usrName)
        return Reply.success(CommentUserInfo.fromDO(rawInfo))
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun addCommentWithPid(@RequestBody @Validated(Insert::class) comment: CommentDTO
    ): Reply<Int> {
        val commentDO = comment.transToDO()
        commentService.addComment(commentDO)
        return Reply.success(commentDO.id!!)
    }

    @GetMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getComments(@RequestParam(value = "start", defaultValue = "1") start: Int,
                    @RequestParam(value = "size", defaultValue = "20") size: Int,
                    @RequestParam(value = "uid") uid: Int?
    ): Reply<Page4Navigator<CommentDO>> {
        logger.info(" uid: {}", uid)
        val pages = commentService.getComments(Page(start.toLong(), size.toLong()), 5, uid)
        return Reply.success(pages)
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun editComment(@PathVariable(name = "id") cmId: Int,
                    @RequestBody @Validated(Update::class) comment: CommentDTO
    ) {
        commentService.editComment(cmId, comment.transToDO())
    }

    @PostMapping("/bulk_delete")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun bulkDeletePrep(@RequestBody idList: IdListDTO): Reply<Unit> {
        bulkDelete.prepToDelete(idList.ids, DELETEKEY.COMMENT)
        return Reply.success()
    }

    @GetMapping("/bulk_delete")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getBatchDeleteInfo(): Reply<List<CommentDO>> {
        val ids = bulkDelete.getDeleteInfo(DELETEKEY.COMMENT)
        val resultList = commentService.getByIds(ids.toList())
        return Reply.success(resultList)
    }

    @DeleteMapping("/bulk_delete")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun batchDelete(): Reply<Unit> {
        val ids = bulkDelete.getDeleteInfo(DELETEKEY.COMMENT)
        commentService.bulkDelete(ids.toList())
        return Reply.success()
    }
}