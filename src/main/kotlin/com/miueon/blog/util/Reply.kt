package com.miueon.blog.util

enum class ResultCode(val code: Int, val msg: String) {
    SUCCESS(200, "process succeed."),
    ERROR_PATH(404, "request addr. not exist."),
    ERROR_SERVER(500, "server internal error."),
    UNAUTHORIZED(401, "not yet login, or token expired."),
    FORBIDDEN(403, "No relevant authority.")
}

class Reply<T>(
        var code: Int? = null,
        var msg: String? = null,
        var content: T? = null
) {
    companion object {
        fun <T> success(data: T): Reply<T> {
            return Reply(ResultCode.SUCCESS.code, ResultCode.SUCCESS.msg, data)
        }

        fun <T> success(data: T, msg: String): Reply<T> {
            return Reply(ResultCode.SUCCESS.code, msg, data)
        }

        fun  errorServer(errorCode: ResultCode): Reply<Any> {
            return Reply(errorCode.code, errorCode.msg)
        }

        fun  errorServer(msg: String): Reply<Any> {
            return Reply(ResultCode.ERROR_SERVER.code, msg)
        }

        fun  errorServer(): Reply<Any> {
            return errorServer(ResultCode.ERROR_SERVER)
        }

        fun  errorPath(): Reply<Any> {
            return Reply(ResultCode.ERROR_PATH.code, ResultCode.ERROR_PATH.msg)
        }

        fun  errorPath(msg: String): Reply<Any> {
            return Reply(ResultCode.ERROR_PATH.code, msg)
        }

        /**
         * not login returns
         */
        fun <T> unauthorized(data: T? = null): Reply<T> {
            return Reply(ResultCode.UNAUTHORIZED.code, ResultCode.UNAUTHORIZED.msg, data)
        }

        fun <T> forbidden(data: T? = null): Reply<T> {
            return Reply(ResultCode.FORBIDDEN.code, ResultCode.FORBIDDEN.msg, data)
        }
    }

}

