package com.miueon.blog.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class FrontPageController {
    @GetMapping("/")
    fun index(): String {
        return "redirect:/index"
    }

    @GetMapping("/index")
    fun home(): String {
        return "index"
    }

    @GetMapping("/detail")
    fun detail(): String {
        return "blogdetailpage"
    }

    @GetMapping("/newpost")
    fun newpost(): String {
        return "newpost"
    }
}