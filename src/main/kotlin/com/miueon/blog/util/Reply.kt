package com.miueon.blog.util

import org.springframework.http.HttpStatus



class Reply<T>(
        var code: String? = null,
        var msg: String? = null,
        var payload: T? = null
) {
    companion object {
        val SUCCESS:String = "0"
        val FAILURE = "-1"
        fun success(): Reply<Unit> {
            return success(Unit)
        }

        fun <T> success(obj: T)
                = Reply(code = SUCCESS, msg = "Operation succeed.", payload = obj)

        fun error(code: String, msg: String):Reply<Unit> = Reply(code, msg, Unit)

        fun error(msg:String):Reply<Unit> = Reply(FAILURE, msg, Unit)
    }

}

