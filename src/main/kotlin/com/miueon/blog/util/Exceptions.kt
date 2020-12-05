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
    var httpStatus: HttpStatus
    constructor(cause: Throwable?, status: HttpStatus) : super(cause) {
        this.httpStatus = status
    }
    constructor(message: String?, status: HttpStatus) : super(message) {
        this.httpStatus = status
    }
    constructor(message: String?) : super(message) {
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
    }
    constructor(status: HttpStatus) : super(status.toString()){
        httpStatus = status
    }

    companion object {
        private const val serialVersionUID = -4642753456084299295L
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