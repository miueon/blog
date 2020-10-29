package com.miueon.blog.pojo

import org.springframework.data.elasticsearch.annotations.Document
import java.util.*

class userDto(
        var name: String? = null,
        var isAdmin: Boolean = false
)
@Document(indexName="blog", type="article")
data class postE(
        var id: String? = null,
        var content: String? = null,
        var title: String?= null
)