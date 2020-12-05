package com.miueon.blog.controller

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mpg.CommentDTO
import com.miueon.blog.mpg.IdList
import com.miueon.blog.mpg.model.CommentDO
import com.miueon.blog.service.BulkDelete
import com.miueon.blog.service.CommentService
import com.miueon.blog.service.DELETEKEY
import com.miueon.blog.service.RedisService
import com.miueon.blog.util.ApiException
import com.miueon.blog.util.Page4Navigator
import com.miueon.blog.util.Reply
import com.miueon.blog.validator.Insert
import com.miueon.blog.validator.Update
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
        val bulkDelete: BulkDelete
) {

    @GetMapping("/post")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getCommentsByPid(@RequestParam("pid")
                         @NotBlank(message = "the pid shouldn't be empty") @Min(1) pid: Int)
            : Reply<List<CommentDO>> {
        val result = commentService.getAllCommentOfPost(pid)
        result.map {
            it.usr?.email = null
            it.usr?.url = null
            it.uid = null
        }
        return Reply.success(result)
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
                    @RequestParam(value = "size", defaultValue = "20") size: Int
    ): Reply<Page4Navigator<CommentDO>> {
        val pages = commentService.getComments(Page(start.toLong(), size.toLong()), 5)
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
    fun bulkDeletePrep(@RequestBody idList: IdList): Reply<Unit> {
        bulkDelete.prepToDelete(idList, DELETEKEY.COMMENT)
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