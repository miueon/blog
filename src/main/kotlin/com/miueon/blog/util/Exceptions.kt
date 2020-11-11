package com.miueon.blog.util

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.RuntimeException
import java.util.HashMap
import com.miueon.blog.util.ResultCode

class ApiException : RuntimeException {
    var httpStatus: HttpStatus
    constructor(cause: Throwable?, code: ResultCode) : super(cause) {
        this.httpStatus = HttpStatus.valueOf(code.code)
    }
    constructor(message: String?, code: ResultCode) : super(message) {
        this.httpStatus = HttpStatus.valueOf(code.code)
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
        return when (e.httpStatus.value()) {
            ResultCode.ERROR_PATH.code ->
                ResponseEntity(Reply.errorPath(e.message ?: ResultCode.ERROR_PATH.msg), e.httpStatus)
            ResultCode.ERROR_SERVER.code ->
                ResponseEntity(Reply.errorServer(e.message ?: ResultCode.ERROR_SERVER.msg), e.httpStatus)
            else -> handleException(e)
        }
    }
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Any?> {
        val reply = Reply.errorServer(e.message ?: HttpStatus.INTERNAL_SERVER_ERROR.toString())
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