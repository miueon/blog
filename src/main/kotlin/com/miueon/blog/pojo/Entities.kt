package com.miueon.blog.pojo

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.ToString
import org.bouncycastle.crypto.params.ParametersWithSalt
import org.springframework.data.elasticsearch.annotations.Document
import java.io.Serializable
import java.util.*


class user:Serializable{
    @TableId(type = IdType.AUTO)
    var id: Long = 0
    var email: String = ""
    var name: String = ""
    var password: String? = null
    var salt: String? = null
    @TableField(value = "createdDate")
    var createdDate: Date = Date()
    constructor()
    constructor(
            id: Long,
            email: String,
            name: String,
            password: String,
            salt: String,
            createdDate: Date
    ){
        this.id = id
        this.email = email
        this.name = name
        this.password = password
        this.salt = salt
        this.createdDate = createdDate
    }
    override fun toString() = "User: name=$name, email=$email, password=$password, salt=$salt, " +
            "createdAt=$createdDate"
}

class post {
    @TableId(type = IdType.AUTO)
    var id: Long? = null
   // @JsonProperty("userId")
    var uid: Long? = null
    var title: String? = null
    @TableField(value = "content")
    var body: String? = null
    @TableField(value = "createdDate")
    var createdDate: Date = Date()
    @TableField(exist = false)
    var user: user? = null
    @TableField(exist = false)
    var userName: String? = null
    @TableField(exist = false)
    var createdBy: String? =null

    constructor()

    constructor(id: Long, uid: Long, title: String, body: String, createdDate: Date,
                user: user, userName: String, createdBy: String){
        this.id = id
        this.uid = uid
        this.title = title
        this.body = body
        this.user = user
        this.userName = userName
        this.createdBy = createdBy
        this.createdDate = createdDate
    }
}

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