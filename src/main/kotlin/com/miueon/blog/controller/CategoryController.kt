package com.miueon.blog.controller


import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mpg.model.CategoryDO
import com.miueon.blog.service.CategoryService
import com.miueon.blog.service.PostService
import com.miueon.blog.util.ApiException
import com.miueon.blog.util.Page4Navigator
import com.miueon.blog.util.Reply

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/category")
class CategoryController {
    @Autowired
    lateinit var categoryService: CategoryService
    @Autowired
    lateinit var postService: PostService
    private var logger = LoggerFactory.getLogger(this.javaClass)
    @GetMapping
    fun getCategories(
            @RequestParam(value = "start", defaultValue = "1") start:Int,
            @RequestParam(value = "size", defaultValue = "20") size:Int
    ): ResponseEntity<Reply<Page4Navigator<CategoryDO>>> {
        val categories = categoryService.getCategories(Page(start.toLong(),
                size.toLong()), 5)
        categories.content?.forEach{
            it.postCount = postService.getPostCountByCid(it.id!!)
        }
        val temp = CategoryDO(0, "unClassified")
        temp.postCount = postService.getPostCountByCid(0)
        categories.content?.add(temp)
        return ResponseEntity(Reply.success(categories), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getCategory(@PathVariable id:Int):ResponseEntity<Reply<CategoryDO>> {
        val category = categoryService.findForId(id)
        return ResponseEntity(Reply.success(category), HttpStatus.OK)
    }



    data class CategoryName(val name: String?)
    @PostMapping
    fun addCategory(@RequestBody cName: CategoryName): Reply<Int> {
        if (StringUtils.isBlank(cName.name) or (cName.name == null)) {
            throw ApiException("category name shouldn't be empty", HttpStatus.NOT_FOUND)
        }
        val result:CategoryDO =  categoryService.saveCategory(cName.name!!)
        logger.info("the id?: {} ", result.id)
        return Reply.success(result.id!!)
    }

    @PutMapping("/{id}")
    fun editCategory(@PathVariable id:Int,@RequestBody cName:CategoryName):Reply<Unit> {
        if (StringUtils.isNotBlank(cName.name)) {
            categoryService.updateForId(id, cName.name!!)
        } else {
            throw ApiException("category name shouldn't be empty",  HttpStatus.BAD_REQUEST)
        }
        return Reply.success()
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Int): Reply<Unit> {
        categoryService.deleteForId(id)
        return Reply.success()
    }
}