package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mpg.IdList
import com.miueon.blog.mpg.mapper.CommentMapper
import com.miueon.blog.mpg.model.CommentDO
import com.miueon.blog.mpg.model.UserDO
import com.miueon.blog.util.Page4Navigator
import org.springframework.beans.factory.annotation.Autowired

interface CommentService {
    fun addComment(comment:CommentDO)
    fun deleteComment(cmId: Int)
    fun editComment(comment: CommentDO)
    fun getAllCommentOfPost(pid: Int):List<CommentDO>
    fun getComments(page: Page<CommentDO>, navigatePages:Int):Page4Navigator<CommentDO>
    fun getById(id:Int):CommentDO
    fun getByIds(ids:IdList):List<CommentDO>
    fun bulkDelete(ids: IdList)
    fun deleteAllOfPid(pid: Int)
}

class CommentServiceImpl
@Autowired
constructor(val commentMapper: CommentMapper,
            val userService: UserService)
    : CommentService {
    override fun addComment(comment: CommentDO) {
        TODO("Not yet implemented")
    }

    override fun deleteComment(cmId: Int) {
        TODO("Not yet implemented")
    }

    override fun editComment(comment: CommentDO) {
        TODO("Not yet implemented")
    }

    override fun getAllCommentOfPost(pid: Int): List<CommentDO> {
        TODO("Not yet implemented")
    }

    override fun getComments(page: Page<CommentDO>, navigatePages: Int): Page4Navigator<CommentDO> {
        TODO("Not yet implemented")
    }

    override fun getById(id: Int): CommentDO {
        TODO("Not yet implemented")
    }

    override fun getByIds(ids: IdList): List<CommentDO> {
        TODO("Not yet implemented")
    }

    override fun bulkDelete(ids: IdList) {
        TODO("Not yet implemented")
    }

    override fun deleteAllOfPid(pid: Int) {
        TODO("Not yet implemented")
    }
}