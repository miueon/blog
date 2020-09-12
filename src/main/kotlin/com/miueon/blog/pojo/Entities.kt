package com.miueon.blog.pojo

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.ToString
import java.util.*


class user(
        @TableId(type = IdType.AUTO)
        var id: Long,
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
        @TableId(type = IdType.AUTO)
        var id: Long? = null,
        @JsonProperty("userId")
        var uid: Long? = null,
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
        @TableId(type = IdType.AUTO)
        var id: Long? = null,
        @JsonProperty("userId")
        var uid: Long? = null,
        @JsonProperty("postId")
        var pid: Long? = null,
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
        @TableId(type = IdType.AUTO)
        var id: Long,
        var role: String
)

class user_autority(
        @TableId(type = IdType.AUTO)
        var id: Long,
        var uid: Long,
        var pid: Long,

        @TableField(exist = false)
        var authority: authority?,
        @TableField(exist = false)
        var user: user?
)