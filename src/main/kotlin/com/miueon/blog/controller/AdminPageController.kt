package com.miueon.blog.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin")
class AdminPageController {


    @GetMapping()
    fun admin(): String {
        return "admin/index"
    }

    @GetMapping("/blog/category")
    fun blogCategory(): String {
        return "admin/category"
    }

    @GetMapping("/blog/category/add")
    fun addCategory(): String {
        return "admin/addCategory"
    }

    @GetMapping("/blog/post")
    fun blogPost(): String {
        return "admin/blogPost"
    }

    @GetMapping("/blog/post/add")
    fun addPost(): String {
        return  "admin/addPost"
    }

    @GetMapping("/blog/tag")
    fun blogTag(): String {
        return "admin/blogTag"
    }

    @GetMapping("/blog/tag/add")
    fun addTag(): String {
        return "admin/addTag"
    }


    @GetMapping("/comments/comment")
    fun comment(): String {
        return "admin/comment"
    }

    @GetMapping("/comments/comment/add")
    fun addComment(): String {
        return "admin/addComment"
    }

}