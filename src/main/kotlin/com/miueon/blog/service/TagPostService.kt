package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.miueon.blog.mpg.mapper.PostMapper
import com.miueon.blog.mpg.mapper.PostTagsMapper
import com.miueon.blog.mpg.mapper.TagsMapper
import com.miueon.blog.mpg.model.PostDO
import com.miueon.blog.mpg.model.PostTagsDO
import com.miueon.blog.mpg.model.TagsDO
import com.miueon.blog.mpg.IdList
import com.miueon.blog.mpg.mapper.CategoryMapper
import com.miueon.blog.mpg.model.CategoryDO
import com.miueon.blog.util.ApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import kotlin.reflect.KMutableProperty1

interface TagPostService {
    fun getTagListByPostId(pid: Int): List<TagsDO>
    fun getPostListByTagId(tid: Int): List<PostDO>
    fun getPostIdsByTags(idList: IdList): IdList
    fun getPostCountByTid(tid: Int): Int
    fun savePostTagRel(pid: Int, tagIdList: List<Int>)
    fun deleteByPid(pid: Int)
    fun deleteByTid(tid: Int)
}

@Service
class TagPostServiceImpl : TagPostService {
    @Autowired
    lateinit var tagMapper: TagsMapper

    @Autowired
    lateinit var postMapper: PostMapper
    @Autowired
    lateinit var categoryMapper: CategoryMapper
    @Autowired
    lateinit var postTagsMapper: PostTagsMapper

    @Autowired
    lateinit var commentService: CommentService

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun getPostIdsByTags(idList: IdList): IdList {
        val idSet: MutableSet<Int> = HashSet()
        idList.forEach { item ->
            val ktQueryWrapper = KtQueryWrapper(PostTagsDO::class.java)
            ktQueryWrapper.eq(PostTagsDO::tid, item)
            idSet.addAll(postTagsMapper.selectList(ktQueryWrapper).map { it.pid })
        }
        return idSet.toList()
    }

    override fun getPostCountByTid(tid: Int): Int {
        val ktQueryWrapper = KtQueryWrapper(PostTagsDO::class.java)
        ktQueryWrapper.eq(PostTagsDO::tid, tid)
        return postTagsMapper.selectCount(ktQueryWrapper)
    }

    override fun getTagListByPostId(pid: Int): List<TagsDO> {
        return getById(pid, Either.TAGBYPOST)
    }

    enum class Either(
            val sid: KMutableProperty1<PostTagsDO, Int>,
            val tid: KMutableProperty1<PostTagsDO, Int>

    ) {
        TAGBYPOST(PostTagsDO::pid, PostTagsDO::tid),
        POSTBYTAG(PostTagsDO::tid, PostTagsDO::pid)
    }

    private fun <T> getById(id: Int, fields: Either): List<T> {
        try {
            val ktQueryWrapper = KtQueryWrapper(PostTagsDO::class.java)
//            var field: KMutableProperty1<PostTagsDO, Int>
//            var mapper:BaseMapper<T>
            val mapper = when (fields) {
                Either.TAGBYPOST -> tagMapper
                Either.POSTBYTAG -> postMapper
            }

            ktQueryWrapper.eq(fields.sid, id)
            val relList = postTagsMapper.selectList(ktQueryWrapper)
            logger.info(" relList: {}", relList)
            return if (relList.size > 0) {
                val result = mapper.selectBatchIds(relList.map(fields.tid).toList()) as List<T>
                logger.info(" the DO result: {}", result)
                result
            } else {
                ArrayList()
            }
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    override fun getPostListByTagId(tid: Int): List<PostDO> {
        val result: List<PostDO> = getById(tid, Either.POSTBYTAG)
        result.forEach {
            it.category = when (it.cid) {
                null -> CategoryDO.unclassified
                else -> categoryMapper.selectById(it.cid)
            }
            it.commentCounts = commentService.getCountsByPid(it.id!!)
        }
        return result.sortedByDescending { it.createdDate }
    }

    private fun isPidExist(pid: Int): Boolean {
        val ktQueryWrapper = KtQueryWrapper(PostTagsDO::class.java)
        ktQueryWrapper.eq(PostTagsDO::pid, pid)
        val result = postTagsMapper.selectCount(ktQueryWrapper)
        return result != 0
    }

    @Transactional
    override fun savePostTagRel(pid: Int, tagIdList: List<Int>) {
        try {
            if (isPidExist(pid)) {
                deleteByPid(pid)
            }
            if (tagIdList.isNotEmpty()) {
                val validateTagsIds = tagMapper.selectBatchIds(tagIdList).map { it.id }.toList()
                val insertList = validateTagsIds
                        .map {
                            val temp = PostTagsDO()
                            temp.pid = pid
                            temp.tid = it!!
                            temp
                        }.toList()
                insertList.forEach { postTagsMapper.insert(it) }
            }
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }

    @Transactional
    override fun deleteByPid(pid: Int) {
        try {
            val ktQueryWrapper = KtQueryWrapper(PostTagsDO::class.java)
            ktQueryWrapper.eq(PostTagsDO::pid, pid)
            postTagsMapper.delete(ktQueryWrapper)
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }

    @Transactional
    override fun deleteByTid(tid: Int) {
        try {
            val ktQueryWrapper = KtQueryWrapper(PostTagsDO::class.java)
            ktQueryWrapper.eq(PostTagsDO::tid, tid)
            postTagsMapper.delete(ktQueryWrapper)
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}