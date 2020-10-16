package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mapper.PostMapper
import com.miueon.blog.mapper.UserMapper
import com.miueon.blog.pojo.post
import com.miueon.blog.util.Page4Navigator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Service
class PostService(@Autowired
                  var postMapper: PostMapper,
                  @Autowired
                  var userMapper: UserMapper,
                  @Autowired
                  var userService: UserService,
                  @Autowired
                  var commentService: CommentService,
                  @Autowired
                  var postEService: PostEService
) {

    var downloadMdPath :String = "E:/0.PROJECT/fullstack/Blog/src/main/resources/static/md"

    fun findForId(id: Long): post? {
        val result = postMapper.selectById(id)
        // read from {id}.md file. may be replace it by store md file into database?
        result.body = readBodyFromMdFile(result.id!!)
        result.user = userMapper.selectById(result?.uid)
        result.user?.password = null
        result.userName = result.user?.name
        return result
    }
    private fun readBodyFromMdFile(id: Long): String {
        val file = File("$downloadMdPath${File.separator}${id}.md")

        val reader = BufferedReader(InputStreamReader(file.inputStream(),
                StandardCharsets.UTF_8))
        val buffer = StringBuffer()
        try {
            var line = reader.readLine()
            while (line != null) {
                buffer.append(line)
                buffer.append("\n")
                line = reader.readLine()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reader.close()
        }

        return buffer.toString()
    }


    fun findForIds(page: Page<post>, id: List<Long>): List<post> {
        val result = postMapper.selectByIDs(page, id)
        return result.records
    }

    fun addPost(title: String): Long {
        val post = post()
        post.title = title
        val user = userService.getRawUser("crux")
        // todo: add a exception
        post.uid = user?.id
        post.body = "null"
        postMapper.insert(post)
        return post.id!!
    }


    fun updatePost(p: post, pid: Long): Int {
        val originalPost = findForId(pid)
        originalPost?.body = "null"
        val ktUpdateWrapper = KtUpdateWrapper(post::class.java)
        ktUpdateWrapper.set(post::title, p.title).eq(post::id, pid)
        val result = postMapper.update(originalPost, ktUpdateWrapper)
        // update to ES
//        val postes = postE()
//        postes.title = p.title
//        postes.content = p.body
//        postes.id = pid.toString()
//        postEService.updatePostE(postes)

        return result
    }


    fun findAllByOrderByCreatedDateDescPage(page: Page<post>, navigatePages:Int): Page4Navigator<post> {
        val ktQueryWrapper = KtQueryWrapper<post>(post::class.java)
        ktQueryWrapper.orderByDesc(post::createdDate)
        val result = postMapper.selectPage(page, ktQueryWrapper)
        result.records.map {
            it.user = userMapper.selectById(it.uid)
            it.userName = it.user?.name
            it.user = null
            if (it.body == "null") {
                it.body = readBodyFromMdFile(it.id!!)

            }
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
       // postEService.deletePostE(id.toString())
    }
}