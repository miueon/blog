package com.miueon.blog.util

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.RuntimeException
import java.util.HashMap

class ApiException : RuntimeException {
    var httpStatus: HttpStatus? = null
    constructor(cause: Throwable?, httpStatus: HttpStatus?) : super(cause) {
        this.httpStatus = httpStatus
    }
    constructor(message: String?, httpStatus: HttpStatus?) : super(message) {
        this.httpStatus = httpStatus
    }
    constructor(message: String?) : super(message) {
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
    }
    companion object {
        private const val serialVersionUID = -4642753456084299295L
    }
}

@ControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<Any?> {
        val reply = Reply(e.httpStatus!!.value(), e.message)
        return ResponseEntity(reply, e.httpStatus!!)
    }
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Any?> {
        val reply = Reply(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.message)
        return ResponseEntity(reply, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}

/////////
@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(val resourceName: String?, val fieldName: String?, val fieldValue: Any?)
    : RuntimeException(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue))