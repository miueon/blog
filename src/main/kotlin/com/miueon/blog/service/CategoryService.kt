package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mpg.mapper.CategoryMapper
import com.miueon.blog.mpg.model.CategoryDO
import com.miueon.blog.util.ApiException
import com.miueon.blog.util.Page4Navigator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface CategoryService {
    fun getCategories(page: Page<CategoryDO>, navigatePages: Int): Page4Navigator<CategoryDO>
    fun findForId(id: Long): CategoryDO
    fun saveCategory(name: String): CategoryDO
    fun updateForId(id: Long, name: String): Int
    fun deleteForId(id: Long)
}

@Service
class CategoryServiceImpl : CategoryService {
    @Autowired
    lateinit var categoryMapper: CategoryMapper

    override fun getCategories(page: Page<CategoryDO>, navigatePages: Int): Page4Navigator<CategoryDO> {
        val ktQueryWrapper = KtQueryWrapper(CategoryDO::class.java)
        ktQueryWrapper.orderByAsc(CategoryDO::id)
        val result = categoryMapper.selectPage(page, ktQueryWrapper)
        return Page4Navigator(result, navigatePages)
    }

    override fun findForId(id: Long): CategoryDO = categoryMapper.selectById(id)

    private fun findByName(name: String): CategoryDO? {
        val ktQueryWrapper = KtQueryWrapper(CategoryDO::class.java)
        ktQueryWrapper.eq(CategoryDO::name, name)
        return categoryMapper.selectOne(ktQueryWrapper)
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
    override fun updateForId(id: Long, name: String): Int {
        val old = findForId(id)
        val ktUpdateWrapper = KtUpdateWrapper(CategoryDO::class.java)
        ktUpdateWrapper.set(CategoryDO::name, name).eq(CategoryDO::id, id)
        val result = categoryMapper.update(old, ktUpdateWrapper)
        return result
    }

    @Transactional
    override fun deleteForId(id: Long) {
        categoryMapper.deleteById(id)
    }
}