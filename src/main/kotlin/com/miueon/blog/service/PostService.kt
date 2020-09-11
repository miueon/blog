package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mapper.PostMapper
import com.miueon.blog.mapper.UserMapper
import com.miueon.blog.pojo.post
import com.miueon.blog.pojo.user
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(@Autowired
                  var postMapper: PostMapper,
                  @Autowired
                  var userMapper: UserMapper
) {


    fun findForId(id: Int): post? {
        val result = postMapper.selectById(id)
        result.user = userMapper.selectById(result?.uid)
        result.userName = result.user?.name
        return result
    }

    fun registerPost(p: post, usr: user): post {
        var newPost = post()
        newPost.title = p.title
        newPost.content = p.content
        newPost.id = p.id
        newPost.user = p.user
        newPost.uid = p.uid
        postMapper.insert(newPost)
        return newPost
    }

    // TODO: after add shiro fun editPost(eP: post)

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


    // TODO: fun deletePost(id: Int): Unit
}