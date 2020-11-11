package com.miueon.blog.controller


import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.pojo.category
import com.miueon.blog.service.CategoryService
import com.miueon.blog.util.ApiException
import com.miueon.blog.util.Page4Navigator
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/category")
class CategoryController {
    @Autowired
    lateinit var categoryService: CategoryService

    @GetMapping
    fun getCategories(
            @RequestParam(value = "start", defaultValue = "1") start:Int,
            @RequestParam(value = "size", defaultValue = "10") size:Int
    ): ResponseEntity<Page4Navigator<category>> {
        val categories = categoryService.getCategories(Page(start.toLong(), size.toLong()), 5)
        return ResponseEntity(categories, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getCategory(@PathVariable id:Long):ResponseEntity<category> {
        val category = categoryService.findForId(id)
        return ResponseEntity(category, HttpStatus.OK)
    }

    @PostMapping
    fun addCategory(name: String?): ResponseEntity<Any> {
        if (name == null || StringUtils.isBlank(name)) {
            throw ApiException("category name shouldn't be empty", HttpStatus.BAD_REQUEST)
        }
        categoryService.saveCategory(name)
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @PutMapping("/{id}")
    fun editCategory(@PathVariable id:Long, name:String):ResponseEntity<Any> {
        if (StringUtils.isNotBlank(name)) {
            categoryService.updateForId(id, name)
        } else {
            throw ApiException("category name shouldn't be empty", HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Any> {
        categoryService.deleteForId(id)
        return ResponseEntity(HttpStatus.OK)
    }
}