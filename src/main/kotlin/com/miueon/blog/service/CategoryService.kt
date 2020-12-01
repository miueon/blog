package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mpg.mapper.CategoryMapper
import com.miueon.blog.mpg.mapper.PostMapper
import com.miueon.blog.mpg.model.CategoryDO
import com.miueon.blog.mpg.model.PostDO
import com.miueon.blog.util.ApiException
import com.miueon.blog.util.Page4Navigator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException

interface CategoryService {
    fun getCategories(page: Page<CategoryDO>, navigatePages: Int): Page4Navigator<CategoryDO>
    fun findForId(id: Int): CategoryDO
    fun saveCategory(name: String): CategoryDO
    fun updateForId(id: Int, name: String)
    fun deleteForId(id: Int)
    fun bulkDelete(ids:Set<Int>)
    fun getDeleteInfo(ids: Set<Int>):List<CategoryDO>
}

@Service
class CategoryServiceImpl : CategoryService {
    companion object{
        private const val unClassified = "unClassified"
    }
    @Autowired
    lateinit var categoryMapper: CategoryMapper
    @Autowired
    lateinit var postMapper: PostMapper
    override fun getCategories(page: Page<CategoryDO>, navigatePages: Int): Page4Navigator<CategoryDO> {
        val ktQueryWrapper = KtQueryWrapper(CategoryDO::class.java)
        ktQueryWrapper.orderByAsc(CategoryDO::id)
        val result = categoryMapper.selectPage(page, ktQueryWrapper)
        return Page4Navigator(result, navigatePages)
    }

    override fun findForId(id: Int): CategoryDO {
        try {
            return categoryMapper.selectById(id)
        } catch (e: RuntimeException) {
            throw ApiException("the id: $id is not in database")
        }
    }

    private fun findByName(name: String): CategoryDO? {
        if (name == unClassified) {
            return CategoryDO(0, unClassified)
        }
        val ktQueryWrapper = KtQueryWrapper(CategoryDO::class.java)
        ktQueryWrapper.eq(CategoryDO::name, name)
        return categoryMapper.selectOne(ktQueryWrapper)
    }

    override fun getDeleteInfo(ids: Set<Int>): List<CategoryDO> {
        try {
            return categoryMapper.selectBatchIds(ids)
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.BAD_REQUEST)
        }
    }

    //    @Transactional(rollbackFor = {RuntimeException.class, IOException.class})
    @Transactional
    override fun saveCategory(name: String): CategoryDO {
        if (findByName(name) != null) {
            throw ApiException("category already exist.", HttpStatus.BAD_REQUEST)
        }

        val result = CategoryDO(id = null, name = name)
        categoryMapper.insert(result)
        return result
    }

    @Transactional
    override fun updateForId(id: Int, name: String) {
        val old = findForId(id)
        if (findByName(name) != null) {
            throw ApiException("category already exist.", HttpStatus.BAD_REQUEST)
        }
        try {
            val ktUpdateWrapper = KtUpdateWrapper(CategoryDO::class.java)
            ktUpdateWrapper.set(CategoryDO::name, name).eq(CategoryDO::id, id)
            categoryMapper.update(old, ktUpdateWrapper)
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }

    @Transactional
    override fun deleteForId(id: Int) {
        findForId(id)
        try {
            obliterateCategoryInfo(id)
            categoryMapper.deleteById(id)
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }


    @Transactional
    fun obliterateCategoryInfo(cid: Int) {
        try {
            val queryWrapper = KtQueryWrapper(PostDO::class.java)
            queryWrapper.eq(PostDO::cid, cid)
            val oldList = postMapper.selectList(queryWrapper)
            oldList.forEach { it.cid = null }
            val ktUpdateWrapper = KtUpdateWrapper(PostDO::class.java)

            oldList.forEach {
                ktUpdateWrapper.set(PostDO::cid, null)
                    .eq(PostDO::id, it.id)
                postMapper.update(it, ktUpdateWrapper)
            }
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
    @Transactional
    override fun bulkDelete(ids: Set<Int>) {
        try {
            ids.forEach {
                obliterateCategoryInfo(it)
            }
            categoryMapper.deleteBatchIds(ids)
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}