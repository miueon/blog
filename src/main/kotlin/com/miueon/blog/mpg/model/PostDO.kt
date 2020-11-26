package com.miueon.blog.mpg.model

import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.miueon.blog.pojo.user
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime
import java.util.*
import kotlin.system.exitProcess

/**
 * <p>
 *
 * </p>
 *
 * @author miueon
 * @since 2020-11-11
 */
@TableName("post")
@ApiModel(value = "PostDO对象", description = "")
class PostDO {

    @TableId(value = "id", type = IdType.AUTO)
    var id: Int? = null
    var uid: Int? = null
    var title: String? = null
    @TableField(value = "content")
    var body: String? = null
    @TableField("createdDate")
    var createdDate: LocalDateTime =  LocalDateTime.now()
    @TableField("modifiedDate")
    var modifiedDate: LocalDateTime = LocalDateTime.now()
    var cid: Int? = null

    @TableField(exist = false)
    var createdBy: String? =null
    @TableField(exist = false)
    var category:String? = null

    @TableField(exist = false)
    var tags:List<TagsDO>? = null
    constructor()
    constructor(id: Int, uid: Int, title: String, body: String, createdDate: LocalDateTime,
                createdBy: String,
                modifiedDate: LocalDateTime,
                cid: Int

    ){
        this.id = id
        this.uid = uid
        this.title = title
        this.body = body

        this.createdBy = createdBy
        this.createdDate = createdDate
        this.modifiedDate = modifiedDate
        this.cid = cid
    }
    override fun toString(): String {
        return "PostDO{" +
                "id=" + id +
                ", uid=" + uid +
                ", title=" + title +
                ", content=" + body +
                ", createdDate=" + createdDate +
                ", modifiedDate=" + modifiedDate +
                ", cid=" + cid +
                "}"
    }
}

