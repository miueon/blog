package com.miueon.blog.controller

import com.miueon.blog.pojo.userDto
import com.miueon.blog.service.UserService
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Exception
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/auth")
class AuthController
@Autowired
constructor(val userService: UserService) {
    private val log = LoggerFactory.getLogger(this::class.java)

    class tokenR(val token:String)
    @PostMapping("/login")
    fun login(@RequestBody loginInfo: userDto, request: HttpServletRequest,
              response: HttpServletResponse): ResponseEntity<tokenR> {
        val subject = SecurityUtils.getSubject()
        try {
            val token = UsernamePasswordToken(loginInfo.username, loginInfo.password)
            subject.login(token)
            // principal is stored in DbRealm
            val userD = subject.principal as userDto
            val newToken = userService.generateJwtToken(userD.username)
            val tokenToReply = tokenR("Bearer $newToken")
           // response.setHeader("Authorization", "Bearer $newToken")
            return ResponseEntity(tokenToReply, HttpStatus.OK)
        } catch (e: AuthenticationException) {
            log.error("user: {} login failed, reason: {}", loginInfo.username, e.message)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/user")
    fun getUser(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<userDto> {
        return ResponseEntity(SecurityUtils.getSubject().principal as userDto, HttpStatus.OK)
    }
}