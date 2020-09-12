package com.miueon.blog.controller

import com.miueon.blog.pojo.comment
import com.miueon.blog.pojo.userDto
import com.miueon.blog.service.CommentService
import org.apache.shiro.SecurityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class CommentController
@Autowired
constructor(val commentService: CommentService){
    var  log: Logger = LoggerFactory.getLogger(CommentController::class.java)

    @GetMapping("/comments/posts/{postId}")
    fun getComments(@PathVariable postId: Long): ResponseEntity<List<comment>> {
        log.debug("REST request to getComments : {}", postId)
        val comments = commentService.findCommentsByPostId(postId) ?: return ResponseEntity.noContent().build()
        return ResponseEntity(comments, HttpStatus.OK)
    }

    @PostMapping("/comments/posts/{pid}")
    fun registerComment(@PathVariable pid: Long, @RequestBody comm: comment): ResponseEntity<comment> {
        comm.pid = pid
        val userD = SecurityUtils.getSubject().principal as userDto
        val retComment = commentService.registerComment(comm, userD.username!!)
        return ResponseEntity(retComment, HttpStatus.OK)
    }


}