package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.config.RedisConfig
import com.miueon.blog.mapper.PostMapper
import com.miueon.blog.mapper.UserMapper
import com.miueon.blog.pojo.post
import com.miueon.blog.pojo.postE
import com.miueon.blog.pojo.user
import com.miueon.blog.pojo.userDto
import com.miueon.blog.util.Page4Navigator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collector
import java.util.stream.Collectors

@Service
class PostService(@Autowired
                  var postMapper: PostMapper,
                  @Autowired
                  var userMapper: UserMapper,
                  @Autowired
                  var commentService: CommentService,
                  @Autowired
                  var postEService: PostEService
) {


    fun findForId(id: Long): post? {
        val result = postMapper.selectById(id)
        result.user = userMapper.selectById(result?.uid)
        result.userName = result.user?.name
        return result
    }


    fun findForIds(page: Page<post>, id: List<Long>): List<post> {
        val result = postMapper.selectByIDs(page, id)
        return result.records
    }



    fun registerPost(p: post, username: String): post {
        var newPost = post()
        newPost.title = p.title
        newPost.body = p.body
        val ktQueryWrapper = KtQueryWrapper(user::class.java)
        ktQueryWrapper.eq(user::name, username)
        val usr = userMapper.selectOne(ktQueryWrapper)
        newPost.userName = usr.name
        newPost.user = usr
        newPost.uid = usr.id
        postMapper.insert(newPost)
        // register to ES
        val pes = postE()
        pes.id = newPost.id.toString()
        pes.title = newPost.title
        pes.content = newPost.body
        postEService.registerPostE(pes)

        return newPost
    }

    fun updatePost(p: post, pid: Long): post {
        val originalPost = findForId(pid)
        val ktUpdateWrapper = KtUpdateWrapper(post::class.java)
        ktUpdateWrapper.set(post::body, p.body).set(post::title, p.title).eq(post::id, pid)
        val result = postMapper.update(originalPost, ktUpdateWrapper)
        // update to ES
        val postes = postE()
        postes.title = p.title
        postes.content = p.body
        postes.id = pid.toString()
        postEService.updatePostE(postes)

        return findForId(result.toLong())!!
    }


    fun findAllByOrderByCreatedDateDescPage(page: Page<post>, navigatePages:Int): Page4Navigator<post> {
        val ktQueryWrapper = KtQueryWrapper<post>(post::class.java)
        ktQueryWrapper.orderByDesc(post::createdDate)
        val result = postMapper.selectPage(page, ktQueryWrapper)
        result.records.map {
            it.user = userMapper.selectById(it.uid)
            it.userName = it.user?.name
            it.user = null
        }
        val pages = Page4Navigator(result, navigatePages)
        return pages
    }

    fun findByKeyword(keyword: String): List<post>? {
        val pesResult = postEService.search(keyword)
        if (pesResult.isNotEmpty()) {
            val ids = pesResult.map { it.id?.toLong()!! }
            val page = Page<post>(0, 10)
            if (ids.isNotEmpty()) {
                val result = findForIds(page, ids)
                return result
            }
        }
        val ktQueryWrapper = KtQueryWrapper(post::class.java)
        ktQueryWrapper.like(post::title, keyword)
        val page = Page<post>(0, 10)
        val result = postMapper.selectPage(page, ktQueryWrapper).records
        if (result.isEmpty()) {
            return null
        }
        result.map {
            it.user = userMapper.selectById(it.uid)
            it.userName = it.user?.name
        }
        return result
    }


    fun deletePost(id: Long) {
        commentService.deleteCommentByPostId(id)
        postMapper.deleteById(id)
        // delete in ES
        postEService.deletePostE(id.toString())
    }
}