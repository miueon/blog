package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mpg.mapper.PostMapper
import com.miueon.blog.mpg.model.PostDO

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
                  var userService: UserService,
//                  @Autowired
//                  var commentService: CommentService,
                  @Autowired
                  var postEService: PostEService
) {

    var downloadMdPath: String = "E:/0.PROJECT/fullstack/Blog/src/main/resources/static/md"

    fun findForId(id: Int): PostDO? {
        val result = postMapper.selectById(id)
        result.createdBy = userService.selectById(result.uid!!).name
        return result
    }

    fun readBodyFromMdFile(id: Int): String {
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


    fun findForIds(ids: List<Int>): List<PostDO> {
        return postMapper.selectBatchIds(ids)
    }

    // todo: add category support
    fun addPost(title: String): PostDO {
        val post = PostDO()
        post.title = title
        post.uid = userService.getRawUser("crux").id
        postMapper.insert(post)
        return post
    }

    fun saveBody(originPost: PostDO) {
        postMapper.updateById(originPost)
    }


    fun updatePost(p: PostDO, pid: Int): Int {
        val originalPost = postMapper.selectById(pid)
        val ktUpdateWrapper = KtUpdateWrapper(PostDO::class.java)
        ktUpdateWrapper.set(PostDO::title, p.title).eq(PostDO::id, pid)
        val result = postMapper.update(originalPost, ktUpdateWrapper)
        // update to ES
//        val postes = postE()
//        postes.title = p.title
//        postes.content = p.body
//        postes.id = pid.toString()
//        postEService.updatePostE(postes)
        return result
    }


    fun findAllByOrderByCreatedDateDescPage(page: Page<PostDO>, navigatePages: Int): Page4Navigator<PostDO> {
        val ktQueryWrapper = KtQueryWrapper<PostDO>(PostDO::class.java)
        ktQueryWrapper.orderByDesc(PostDO::createdDate)
        val result = postMapper.selectPage(page, ktQueryWrapper)
        result.records.map {
            it.createdBy = userService.selectById(it.uid!!).name
        }
        return Page4Navigator(result, navigatePages)
    }

//    fun findByKeyword(keyword: String): List<PostDTO>? {
//        val pesResult = postEService.search(keyword)
//        if (pesResult.isNotEmpty()) {
//            val ids = pesResult.map { it.id?.toLong()!! }
//            if (ids.isNotEmpty()) {
//                val result = findForIds(ids)
//                return result
//            }
//        }
//        val ktQueryWrapper = KtQueryWrapper(PostDO::class.java)
//        ktQueryWrapper.like(PostDO::title, keyword)
//        val page = Page<PostDO>(0, 10)
//        val result = postMapper.selectPage(page, ktQueryWrapper).records
//        if (result.isEmpty()) {
//            return null
//        }
//        result.map {
//            it.user = userService.selectById(it.uid)
//            it.userName = it.user?.name
//        }
//        return result
//    }


    fun deletePost(id: Int) {
//        commentService.deleteCommentByPostId(id)
        postMapper.deleteById(id)
        // delete in ES
        // postEService.deletePostE(id.toString())
    }
}