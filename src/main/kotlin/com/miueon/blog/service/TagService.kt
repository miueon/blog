package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mpg.mapper.TagsMapper
import com.miueon.blog.mpg.model.TagsDO
import com.miueon.blog.util.ApiException
import com.miueon.blog.util.Page4Navigator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException

interface TagService {
    fun getTags(page: Page<TagsDO>, navigatePages: Int): Page4Navigator<TagsDO>
    fun findForId(id: Int): TagsDO
    fun selectBatchForIds(ids:List<Int>):List<TagsDO>
    fun saveTag(name: String): TagsDO
    fun updateForId(id: Int, name: String)
    fun deleteForId(id: Int)
}

@Service
class TagServiceImpl : TagService {
    @Autowired
    lateinit var tagsMapper: TagsMapper
    @Autowired
    lateinit var tagPostService: TagPostService

    override fun getTags(page: Page<TagsDO>, navigatePages: Int): Page4Navigator<TagsDO> {
        val ktQueryWrapper = KtQueryWrapper(TagsDO::class.java)
        ktQueryWrapper.orderByAsc(TagsDO::id)
        val result = tagsMapper.selectPage(page, ktQueryWrapper)
        return Page4Navigator(result, navigatePages)
    }

    override fun selectBatchForIds(ids: List<Int>): List<TagsDO> {
        return tagsMapper.selectBatchIds(ids)
    }

    override fun findForId(id: Int): TagsDO {
        try {
            return tagsMapper.selectById(id)
        } catch (e: RuntimeException) {
            throw ApiException("the tag id $id is not exist in database.")
        }
    }

    private fun findByName(name: String): TagsDO? {
        val ktQueryWrapper = KtQueryWrapper(TagsDO::class.java)
        ktQueryWrapper.eq(TagsDO::name, name)
        return tagsMapper.selectOne(ktQueryWrapper)
    }

    @Transactional
    override fun saveTag(name: String): TagsDO {
        if (findByName(name) != null) {
            throw ApiException("the tag name: $name already exist in db.", HttpStatus.BAD_REQUEST)
        }

        val result = TagsDO()
        result.name = name
        tagsMapper.insert(result)
        return result
    }

    @Transactional
    override fun updateForId(id: Int, name: String) {
        val old = findForId(id)
        if (findByName(name) != null) {
            throw ApiException("the tag name: $name already exist in db.", HttpStatus.BAD_REQUEST)
        }

        try {
            val ktUpdateWrapper = KtUpdateWrapper(TagsDO::class.java)
            ktUpdateWrapper.set(TagsDO::name, name).eq(TagsDO::id, id)
            tagsMapper.update(old, ktUpdateWrapper)
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @Transactional
    override fun deleteForId(id: Int) {
        findForId(id)
        try {
            tagPostService.deleteByTid(id)
            tagsMapper.deleteById(id)
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}