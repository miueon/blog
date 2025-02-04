package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.aop.CacheTTL
import com.miueon.blog.config.RedisKeys
import com.miueon.blog.mpg.mapper.CategoryMapper
import com.miueon.blog.mpg.mapper.PostMapper
import com.miueon.blog.mpg.model.CategoryDO
import com.miueon.blog.mpg.model.PostDO
import com.miueon.blog.mpg.IdList
import com.miueon.blog.mpg.PostArchive
import com.miueon.blog.mpg.PostTitle
import com.miueon.blog.util.ApiException

import com.miueon.blog.util.Page4Navigator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

@Service
class PostService(@Autowired
                  var postMapper: PostMapper,
                  @Autowired
                  var userService: UserService,
                  @Autowired
                  var tagPostService: TagPostService,
                  @Autowired
                  var commentService: CommentService,
                  @Autowired
                  var categoryMapper: CategoryMapper,
                  @Autowired
                  var redisService: RedisService,
                  @Autowired
                  var redisKeys: RedisKeys
//                  @Autowired
//                  var postEService: PostEService
) {

    var downloadMdPath: String = "E:/0.PROJECT/fullstack/Blog/src/main/resources/static/md"

    fun getLatestPostInfo(): List<PostTitle> {
        return if (redisService.hasKey(redisKeys.latestPost)) {
            return redisService.lRange(redisKeys.latestPost, 0, 4) as List<PostTitle>
        }else {
            val ktQueryWrapper = KtQueryWrapper(PostDO::class.java)
            ktQueryWrapper.orderByDesc(PostDO::createdDate).last("limit 5")
            val result = postMapper.selectList(ktQueryWrapper).map { PostTitle(it.id, it.title) }
            redisService.lrPushAll(redisKeys.latestPost, *result.toTypedArray())
            result
        }
    }

    fun getArchiveData(): List<PostArchive> {
        return if (redisService.hasKey(redisKeys.archiveKey)) {
            redisService[redisKeys.archiveKey] as List<PostArchive>
        } else {
            val ktQueryWrapper = KtQueryWrapper(PostDO::class.java)
            val postList = postMapper.selectList(ktQueryWrapper)
            val archiveSet = HashSet<PostArchive>()
            postList.groupBy {
                it.createdDate.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate()
            }.forEach { (date, value) -> archiveSet.add(PostArchive(date.year, date.monthValue, value.size)) }
            val result = archiveSet.toList().sortedByDescending { it.yearMonth }
            redisService[redisKeys.archiveKey, result] = 3600 * 12
            result
        }
    }

    fun getPostsByDate(page: Page<PostDO>, year: Int, month: Int, pages: Int): Page4Navigator<PostDO> {
        val ktQueryWrapper = KtQueryWrapper(PostDO::class.java)
        ktQueryWrapper.orderByDesc(PostDO::createdDate)
        ktQueryWrapper.apply("month(createdDate) = {0} and year(createdDate) = {1}", month, year)
        return selectPage(page, ktQueryWrapper, pages)
    }

    fun getPostCountByCid(cid: Int): Int {
        val ktQueryWrapper = KtQueryWrapper(PostDO::class.java)
        return when (cid) {
            0 -> {
                ktQueryWrapper.isNull(PostDO::cid)
                postMapper.selectCount(ktQueryWrapper)
            }
            else -> {
                ktQueryWrapper.eq(PostDO::cid, cid)
                postMapper.selectCount(ktQueryWrapper)
            }
        }
    }

    fun getPostByCid(page: Page<PostDO>, cid: Int): Page<PostDO>? {
        val ktQueryWrapper = KtQueryWrapper(PostDO::class.java)
        ktQueryWrapper.orderByDesc(PostDO::createdDate)
        val result = postMapper.selectPage(page, ktQueryWrapper)
        return result
    }

    fun findForId(id: Int): PostDO {
        try {
            val result = postMapper.selectById(id)

            result.createdBy = userService.selectById(result.uid!!).name

            result.category = when (result.cid) {
                null -> CategoryDO(0, "unClassified")
                else -> categoryMapper.selectById(result.cid!!)
            }
            result.commentCounts = commentService.getCountsByPid(id)
            result.tags = tagPostService.getTagListByPostId(result.id!!)
            return result
        } catch (e: ApiException) {
            throw e
        } catch (e: RuntimeException) {
            throw ApiException("the post id: $id is not found.", HttpStatus.BAD_REQUEST)
        }
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
        try {
            return postMapper.selectBatchIds(ids)
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.BAD_REQUEST)
        }
    }

    // todo: add category support
//    fun addPost(title: String): PostDO {
//        val post = PostDO()
//        post.title = title
//        post.uid = userService.getRawUser("crux").id
//        postMapper.insert(post)
//        return post
//    }

    private inline fun setCid2null(postDO: PostDO) {
        if (postDO.cid == 0) {
            postDO.cid = null
        }
    }

    @Transactional
    fun savePost(postDO: PostDO): PostDO {
        try {
            postDO.uid = userService.getRawUser("miueon").id
            val ktQueryWrapper = KtQueryWrapper(PostDO::class.java)
            ktQueryWrapper.eq(PostDO::title, postDO.title)
            if (postMapper.selectCount(ktQueryWrapper) > 0) {
                throw ApiException(
                        "duplicated title for ${postDO.title}",
                        HttpStatus.BAD_REQUEST
                )
            }
            setCid2null(postDO)

            postMapper.insert(postDO)
            redisService.lrPop(redisKeys.latestPost)
            redisService.llPush(redisKeys.latestPost, PostTitle(postDO.id, postDO.title))
            redisService.del(redisKeys.archiveKey)
            return postDO
        } catch (e: ApiException) {
            throw  e
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    fun saveBody(originPost: PostDO) {
        postMapper.updateById(originPost)
    }

    @Transactional
    fun updatePost(p: PostDO, pid: Int): Int {
        try {
            p.modifiedDate = LocalDateTime.now()
            val ktUpdateWrapper = KtUpdateWrapper(PostDO::class.java).eq(PostDO::id, pid)
            setCid2null(p)
            if (p.cid != null) {
                categoryMapper.selectByPrimaryKey(p.cid)
                        ?: throw ApiException("the category id: ${p.cid} is not exist.",
                                HttpStatus.BAD_REQUEST)
            }

            val result = postMapper.update(p, ktUpdateWrapper)
            // update to ES
//        val postes = postE()
//        postes.title = p.title
//        postes.content = p.body
//        postes.id = pid.toString()
//        postEService.updatePostE(postes)
            return result
        } catch (e: ApiException) {
            throw  e
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }


    fun findAllByOrderByCreatedDateDescPage(page: Page<PostDO>, navigatePages: Int, cid: Int?
    ): Page4Navigator<PostDO> {
        val ktQueryWrapper = KtQueryWrapper(PostDO::class.java)
        if (cid != null) {
            when (cid) {
                0 -> ktQueryWrapper.isNull(PostDO::cid)
                else -> ktQueryWrapper.eq(PostDO::cid, cid)
            }
        }

        ktQueryWrapper.orderByDesc(PostDO::createdDate)
        return selectPage(page, ktQueryWrapper, navigatePages)
    }

    fun polishPostList(posts: List<PostDO>): List<PostDO> {
         posts.forEach {
            it.createdBy = userService.selectById(it.uid!!).name
            it.category = when (it.cid) {
                null -> CategoryDO(0, "unClassified")
                else -> categoryMapper.selectByPrimaryKey(it.cid!!)
            }
            it.tags = tagPostService.getTagListByPostId(it.id!!)
            it.commentCounts = commentService.getCountsByPid(it.id!!)
        }
        return posts
    }

    private inline fun selectPage(page: Page<PostDO>, ktQueryWrapper: KtQueryWrapper<PostDO>, navigatePages: Int)
            : Page4Navigator<PostDO> {
        val result = postMapper.selectPage(page, ktQueryWrapper)
        result.records.map {
            it.createdBy = userService.selectById(it.uid!!).name
            it.category = when (it.cid) {
                null -> CategoryDO(0, "unClassified")
                else -> categoryMapper.selectByPrimaryKey(it.cid!!)
            }
            it.tags = tagPostService.getTagListByPostId(it.id!!)
            it.commentCounts = commentService.getCountsByPid(it.id!!)
        }
        return Page4Navigator(result, navigatePages)
    }

    fun findByIdsOrderByCreatedDateDescPage(page: Page<PostDO>, navigatePages: Int, pids: IdList)
            : Page4Navigator<PostDO> {
        val ktQueryWrapper = KtQueryWrapper(PostDO::class.java)
        ktQueryWrapper.`in`(PostDO::id, pids).orderByDesc(PostDO::createdDate)
        return selectPage(page, ktQueryWrapper, navigatePages)
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

    @Transactional
    fun deletePost(id: Int) {
//        commentService.deleteCommentByPostId(id)
        try {
            tagPostService.deleteByPid(id)
            postMapper.deleteById(id)
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }

        // delete in ES
        // postEService.deletePostE(id.toString())
    }

    @Transactional
    fun bulkDelete(ids: IdList) {
        try {
            ids.forEach {
                tagPostService.deleteByPid(it)
                commentService.deleteAllOfPid(it)
            }
            val latest = redisService.lRange(redisKeys.latestPost, 0, 4) as List<PostTitle>
            val idsSet = ids.toSet()
            for (item in latest) {
                if (idsSet.contains(item.id)) {
                    redisService.del(redisKeys.latestPost)
                }
            }
            redisService.del(redisKeys.archiveKey)
            postMapper.deleteBatchIds(ids)
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

}