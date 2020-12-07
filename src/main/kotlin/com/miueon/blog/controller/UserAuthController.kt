package com.miueon.blog.controller


import com.miueon.blog.mpg.CommentUserInfo
import com.miueon.blog.service.UserService
import com.miueon.blog.util.Reply
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.constraints.NotBlank
import kotlin.streams.toList

@RestController
@RequestMapping("/auth")
class UserAuthController{

    data class userDto(var isAdmin:Boolean? = null, var name:String? = null)

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

    @GetMapping("/check_user")
    fun checkUser(@RequestParam("name")@NotBlank name:String): ResponseEntity<Reply<Unit>> {
        return ResponseEntity(Reply.success(), HttpStatus.OK)
    }

    @GetMapping("/admin")
    fun getAdmin(): ResponseEntity<userDto> {
        return sharedReturnInfo()
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun regist(@RequestBody @Validated user: CommentUserInfo):userDto {
        val usr = user.transToDO()
        usr.password = usr.email
        userService.addUser(usr)
        return userDto(false, user.name)
    }
}