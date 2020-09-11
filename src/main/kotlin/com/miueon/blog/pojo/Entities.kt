package com.miueon.blog.pojo

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.ToString
import java.util.*


class user(
        @TableId
        var id: Int,
        var email: String,
        var name: String,
        var password: String,
        var salt: String?,
        @TableField(value = "createdDate")
        var createdDate: Date = Date()
) {
    override fun toString() = "User: name=$name, email=$email, password=$password, salt=$salt, " +
            "createdAt=$createdDate"
}

class post(
        @TableId
        var id: Int? = null,
        @JsonProperty("userId")
        var uid: Int? = null,
        var title: String? = null,
        @JsonProperty("body")
        var content: String? = null,
        @TableField(value = "createdDate")
        var createdDate: Date = Date(),
        @TableField(exist = false)
        var user: user? = null,
        @TableField(exist = false)
        var userName: String? = null,
        @TableField(exist = false)
        var createdBy: String? =null
)

class comment(
        @TableId
        var id: Int? = null,
        @JsonProperty("userId")
        var uid: Int? = null,
        @JsonProperty("postId")
        var pid: Int? = null,
        @TableField(value = "createdDate")
        var createdDate: Date = Date(),
        @JsonProperty("body")
        var content: String? = null,
        @TableField(exist = false)
        var user: user? = null,
        @TableField(exist = false)
        var post: post? = null,
        @TableField(exist = false)
        var userName: String? = null
)

class authority(
        @TableId
        var id: Int,
        var role: String
)

class user_autority(
        @TableId
        var id: Int,
        var uid: Int,
        var pid: Int,

        @TableField(exist = false)
        var authority: authority?,
        @TableField(exist = false)
        var user: user?
)