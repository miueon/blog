package com.miueon.blog.controller

import com.miueon.blog.pojo.user
import com.miueon.blog.pojo.userDto
import com.miueon.blog.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.streams.toList

@RestController
@RequestMapping("/auth")
class UserAuthController{
    @Autowired
    lateinit var userService: UserService

    private fun sharedReturnInfo():ResponseEntity<userDto> {
        val user = userDto()
        val authentication = SecurityContextHolder.getContext().authentication
//        val roles = authentication.authorities.stream().map { it.authority }.toList()
//        println("roles $roles")
        user.isAdmin = authentication.authorities.stream().anyMatch{it.authority == "ROLE_ADMIN"}
        user.name = authentication.name
        return ResponseEntity(user, HttpStatus.OK)
    }

    @GetMapping("/user")
    fun getUser():ResponseEntity<userDto> {
        return sharedReturnInfo()
    }

    @GetMapping("/admin")
    fun getAdmin(): ResponseEntity<userDto> {
        return sharedReturnInfo()
    }
}