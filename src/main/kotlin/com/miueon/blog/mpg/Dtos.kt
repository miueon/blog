package com.miueon.blog.mpg

import com.miueon.blog.mpg.model.UserDO
import org.springframework.data.elasticsearch.annotations.Document
import java.util.*

class userDto(
        var name: String? = null,
        var isAdmin: Boolean = false
)

class CommentUserInfo {
    var name: String? = null
    var email: String? = null
    var url: String? = null

    fun toUserDO():UserDO {
        val usr = UserDO()
        usr.name = name
        usr.email = email
        usr.url = url
        return usr
    }

    companion object {
        fun fromUserDO(usr: UserDO): CommentUserInfo {
            val cUsr = CommentUserInfo()
            cUsr.name = usr.name
            cUsr.email = usr.email
            cUsr.url = usr.url
            return cUsr
        }

    }

}

@Document(indexName="blog", type="article")
data class postE(
        var id: String? = null,
        var content: String? = null,
        var title: String?= null
)


typealias IdList = List<Int>