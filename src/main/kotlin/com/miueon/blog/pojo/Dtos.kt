package com.miueon.blog.pojo

import org.springframework.data.elasticsearch.annotations.Document
import java.util.*

class userDto(
        var id: Long? = null,
        var email: String? = null,
        var password: String? = null,
        var username: String? = null,
        var authorities: Set<String>? = null,
        var createdDate: Date = Date(),
        var salt: String? = null
)
@Document(indexName="blog", type="article")
data class postE(
        var id: String? = null,
        var content: String? = null,
        var title: String?= null
)