package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.pojo.category
import com.miueon.blog.util.Page4Navigator
import org.springframework.stereotype.Service

interface CategoryService {
    fun getCategories(page: Page<category>, navigatePages:Int):Page4Navigator<category>
    fun findForId(id:Long):category
    fun saveCategory(name:String)
    fun updateForId(id:Long, name: String)
    fun deleteForId(id: Long)
}
@Service
class categoryServiceImpl : CategoryService {
    override fun getCategories(page: Page<category>, navigatePages: Int): Page4Navigator<category> {
        TODO("Not yet implemented")
    }

    override fun findForId(id: Long): category {
        TODO("Not yet implemented")
    }

    override fun saveCategory(name: String) {
        TODO("Not yet implemented")
    }

    override fun updateForId(id: Long, name: String) {
        TODO("Not yet implemented")
    }

    override fun deleteForId(id: Long) {
        TODO("Not yet implemented")
    }
}