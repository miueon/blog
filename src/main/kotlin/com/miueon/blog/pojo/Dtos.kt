package com.miueon.blog.pojo

import java.util.*

class userDto(
        var id: Int? = null,
        var email: String? = null,
        var password: String? = null,
        var username: String? = null,
        var authorities: Set<String>? = null,
        var createdDate: Date = Date(),
        var salt: String? = null
)