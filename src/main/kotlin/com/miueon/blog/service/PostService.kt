package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mapper.PostMapper
import com.miueon.blog.mapper.UserMapper
import com.miueon.blog.pojo.post
import com.miueon.blog.pojo.user
import com.miueon.blog.pojo.userDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(@Autowired
                  var postMapper: PostMapper,
                  @Autowired
                  var userMapper: UserMapper,
                  @Autowired
                  var commentService:CommentService
) {


    fun findForId(id: Long): post? {
        val result = postMapper.selectById(id)
        result.user = userMapper.selectById(result?.uid)
        result.userName = result.user?.name
        return result
    }

    fun registerPost(p: post, username: String): post {
        var newPost = post()
        newPost.title = p.title
        newPost.content = p.content
        val ktQueryWrapper = KtQueryWrapper(user::class.java)
        ktQueryWrapper.eq(user::name, username)
        val usr = userMapper.selectOne(ktQueryWrapper)
        newPost.userName = usr.name
        newPost.user = usr
        newPost.uid = usr.id

        postMapper.insert(newPost)
        return newPost
    }

    fun updatePost(p:post, pid: Long):post {
        val originalPost = findForId(pid)
        val ktUpdateWrapper = KtUpdateWrapper(post::class.java)
        ktUpdateWrapper.set(post::content, p.content).set(post::title, p.title).eq(post::id, pid)
        val result = postMapper.update(originalPost, ktUpdateWrapper)
        return findForId(result.toLong())!!
    }


    fun findAllByOrderByCreatedDateDescPage(p: Page<post>): List<post> {
        val ktQueryWrapper = KtQueryWrapper<post>(post::class.java)
        ktQueryWrapper.orderByDesc(post::createdDate)
        val result = postMapper.selectPage(p, ktQueryWrapper).records
        result.map {
            it.user = userMapper.selectById(it.uid)
            it.userName = it.user?.name
        }
        return result
    }


    fun deletePost(id: Long) {
        commentService.deleteCommentByPostId(id)
        postMapper.deleteById(id)
    }
}