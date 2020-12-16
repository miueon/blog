package com.miueon.blog.controller


import com.miueon.blog.mpg.CommentUserInfo
import com.miueon.blog.service.UserService
import com.miueon.blog.util.Reply
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/auth")
class UserAuthController{

    data class UserDto(var isAdmin:Boolean? = null, var name:String? = null)
    data class ChangePwd(val password:String)

    @Autowired
    lateinit var userService: UserService

    private fun sharedReturnInfo():ResponseEntity<UserDto> {
        val user = UserDto()
        val authentication = SecurityContextHolder.getContext().authentication
//        val roles = authentication.authorities.stream().map { it.authority }.toList()
//        println("roles $roles")
        user.isAdmin = authentication.authorities.stream().anyMatch{it.authority == "ROLE_ADMIN"}
        user.name = authentication.name
        return ResponseEntity(user, HttpStatus.OK)
    }

    @GetMapping("/user")
    fun getUser():ResponseEntity<UserDto> {
        return sharedReturnInfo()
    }

    @GetMapping("/check_user")
    fun checkUser(@RequestParam("name")@NotBlank name:String): ResponseEntity<Reply<Unit>> {
        return ResponseEntity(Reply.success(), HttpStatus.OK)
    }

    @GetMapping("/admin")
    fun getAdmin(): ResponseEntity<UserDto> {
        return sharedReturnInfo()
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun regist(@RequestBody @Validated user: CommentUserInfo):UserDto {
        val usr = user.transToDO()
        usr.password = usr.email
        userService.addUser(usr)
        return UserDto(false, user.name)
    }

    @PostMapping("/admin/change_pwd")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun changePwd(@RequestBody pwd: ChangePwd):Reply<Unit> {
        userService.changePwd(pwd.password)
        return Reply.success()
    }
}