package com.miueon.blog.util

import java.io.Serializable
enum class CodeEnum(val code: Int, val msg: String) {
    SUCCESS(200, "process succeed."),
    ERROR_PATH(404, "request addr. not exist."),
    ERROR_SERVER(505, "server internal error.")
}
class Reply(
        var code: Int,
        var msg: String,
        var data:Any?
) {
    fun fillCode(codeEnum: CodeEnum):Reply {
        this.code = codeEnum.code
        this.msg = codeEnum.msg
        return this
    }

    fun fillCode(code: Int, msg: String): Reply {
        this.code = code
        this.msg = msg
        return this
    }

    fun fillData(data: Any?):Reply {
        this.code = CodeEnum.SUCCESS.code
        this.msg = CodeEnum.SUCCESS.msg
        this.data = data
        return this
    }
}

