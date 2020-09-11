package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.miueon.blog.exceptions.BadRequestException
import com.miueon.blog.mapper.CommentMapper
import com.miueon.blog.mapper.PostMapper
import com.miueon.blog.mapper.UserMapper
import com.miueon.blog.pojo.comment
import com.miueon.blog.pojo.post
import com.miueon.blog.pojo.user
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalArgumentException

@Service
class CommentService
@Autowired
constructor(
        val commentMapper: CommentMapper,
        val postMapper: PostMapper,
        val usrMapper: UserMapper
) {
    fun findById(id: Int): comment? {
        return commentMapper.selectById(id)
    }

    fun findPostByCid(pid: Int): post? {
        return postMapper.selectById(pid)
    }

    fun findCommentsByPostId(pid: Int): List<comment>? {
        val ktQueryWrapper = KtQueryWrapper<comment>(comment::class.java)
        ktQueryWrapper.eq(comment::pid, pid)
        val reuslt = commentMapper.selectList(ktQueryWrapper)
        fillCommentsUser(reuslt)
        return reuslt
    }

    fun fillCommentsUser(coms: List<comment>): Unit {
        coms.map {
            it.user= usrMapper.selectById(it.uid)
            it.userName = it.user?.name
        }
    }

    fun registerComment(comm: comment, usr: user): comment? {
        val postInctx: post = findPostByCid(comm.pid ?: throw BadRequestException("request without post id"))
                ?: throw BadRequestException("post doesnt exist in database")
        val newComment = comment(
                content = comm.content,
                pid = comm.pid,
                post = postInctx,
                uid = usr.id
        )
        commentMapper.insert(newComment)
        newComment.user=usr
        return newComment
    }

    fun deleteComment(id: Int): Unit {
        commentMapper.selectById(id)?.let {
            commentMapper.deleteById(it.id)
        }
    }

}