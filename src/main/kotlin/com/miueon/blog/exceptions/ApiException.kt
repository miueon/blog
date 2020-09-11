package com.miueon.blog.exceptions

import org.springframework.http.HttpStatus


class ApiException:RuntimeException {
    private  var exceptionCode: String? = null
    private  var httpStatus: HttpStatus? = null

    constructor(cause: Throwable, httpStatus: HttpStatus) : super(cause){
        this.httpStatus = httpStatus
    }

    constructor(msg: String, httpStatus: HttpStatus) : super(msg){
        this.httpStatus = httpStatus
    }

    constructor(msg: String) : super(msg){
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    }
}