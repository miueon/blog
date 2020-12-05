package com.miueon.blog.aop

import com.miueon.blog.util.ApiException
import com.miueon.blog.util.Reply
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.validation.ConstraintViolationException
import kotlin.text.StringBuilder

@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<Any?> {
        return when (e.httpStatus) {
            HttpStatus.NOT_FOUND ->
                ResponseEntity(Reply.error(e.message ?: e.httpStatus.toString()), e.httpStatus)
            HttpStatus.INTERNAL_SERVER_ERROR ->
                ResponseEntity(Reply.error(e.message ?: e.httpStatus.toString()), e.httpStatus)
            HttpStatus.BAD_REQUEST ->
                ResponseEntity(Reply.error(e.message ?: e.httpStatus.toString()), e.httpStatus)
            else -> handleException(e)
        }
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleParamOrPath(e: ConstraintViolationException): Reply<Unit> {
        val msg = with(StringBuilder()) {
            this.append("Request Param or path variable is not valid.")
            for (s in e.constraintViolations) {
                this.append("${s.invalidValue}: ${s.message}")
            }
            this.toString()
        }
        return Reply.error(msg)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Any?> {
        val reply = Reply.error(HttpStatus.INTERNAL_SERVER_ERROR.toString())
        return ResponseEntity(reply, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}