package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mpg.IdList
import com.miueon.blog.mpg.mapper.CommentMapper
import com.miueon.blog.mpg.mapper.PostMapper
import com.miueon.blog.mpg.model.CommentDO
import com.miueon.blog.mpg.model.UserDO
import com.miueon.blog.util.ApiException
import com.miueon.blog.util.Page4Navigator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface CommentService {
    fun addComment(comment:CommentDO)
    fun editComment(cmId:Int, comment: CommentDO)
    fun getAllCommentOfPost(pid: Int):List<CommentDO>
    fun getComments(page: Page<CommentDO>, navigatePages:Int):Page4Navigator<CommentDO>
    fun getById(id:Int):CommentDO
    fun getCountsByPid(pid: Int):Int

    fun getByIds(ids:IdList):List<CommentDO>
    fun bulkDelete(ids: IdList)

    fun deleteAllOfPid(pid: Int)
}
@Service
class CommentServiceImpl
@Autowired
constructor(val commentMapper: CommentMapper,
            val postMapper: PostMapper,
            val userService: UserService)
    : CommentService {
    override fun getCountsByPid(pid: Int): Int {
        val ktQueryWrapper = KtQueryWrapper(CommentDO::class.java)
        ktQueryWrapper.eq(CommentDO::pid, pid)
        return commentMapper.selectCount(ktQueryWrapper)
    }

    @Transactional
    override fun addComment(comment: CommentDO) {
        val userName = comment.usr
        val userDO =  userService.getRawUser(userName)
        comment.uid = userDO.id
        if (postMapper.selectById(comment.pid!!) == null) {
            throw ApiException("pid invalid.", HttpStatus.BAD_REQUEST)
        }
        commentMapper.insert(comment)
    }

    @Transactional
    override fun editComment(cmId:Int,comment: CommentDO) {
        val oldComment = commentMapper.selectById(cmId) ?: throw ApiException("comment id is invalid.")
        oldComment.content = comment.content
        commentMapper.updateById(oldComment)
    }

    override fun getAllCommentOfPost(pid: Int): List<CommentDO> {
        val ktQueryWrapper = KtQueryWrapper(CommentDO::class.java)
        ktQueryWrapper.eq(CommentDO::pid, pid)
        ktQueryWrapper.orderByDesc(CommentDO::createdDate)
        val resultList =  commentMapper.selectList(ktQueryWrapper)
        resultList.forEach {
            it.usr = userService.selectById(it.uid!!).name
        }
        return resultList
    }

    override fun getComments(page: Page<CommentDO>, navigatePages: Int): Page4Navigator<CommentDO> {
        val ktQueryWrapper = KtQueryWrapper(CommentDO::class.java)
        ktQueryWrapper.orderByDesc(CommentDO::createdDate)
        val result = commentMapper.selectPage(page, ktQueryWrapper)
        return Page4Navigator(result, navigatePages)
    }

    override fun getById(id: Int): CommentDO {
        return commentMapper.selectById(id) ?: throw ApiException("the comment id is invalid.")
    }

    override fun getByIds(ids: IdList): List<CommentDO> {
        return commentMapper.selectBatchIds(ids)
    }

    @Transactional
    override fun bulkDelete(ids: IdList) {
        commentMapper.deleteBatchIds(ids)
    }

    @Transactional
    override fun deleteAllOfPid(pid: Int) {
        val ktQueryWrapper = KtQueryWrapper(CommentDO::class.java)
        ktQueryWrapper.eq(CommentDO::pid, pid)
        commentMapper.delete(ktQueryWrapper)
    }
}