package com.miueon.blog.mpg.model

import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime
import java.time.ZoneId
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
    var createdDate: LocalDateTime =  LocalDateTime.now(ZoneId.of("+08:00"))
    @TableField("modifiedDate")
    var modifiedDate: LocalDateTime = LocalDateTime.now(ZoneId.of("+08:00"))
    @JsonIgnore
    var cid: Int? = null
    var view: Int = 0

    @TableField(exist = false)
    var createdBy: String? =null
    @TableField(exist = false)
    var category:CategoryDO? = null

    @TableField(exist = false)
    var tags:List<TagsDO>? = null
    @TableField(exist = false)
    var toc:String? = null

    @TableField(exist = false)
    var commentCounts: Int = 0
    constructor()
    constructor(id: Int?, uid: Int?, title: String?, body: String?, createdDate: LocalDateTime?,
                modifiedDate: LocalDateTime?,
                cid: Int?,
                view:Int?
    ){
        this.id = id
        this.uid = uid
        this.title = title
        this.body = body
        this.createdDate = createdDate ?: LocalDateTime.now()
        this.modifiedDate = modifiedDate ?: LocalDateTime.now()
        this.cid = cid
        this.view = view ?: 0
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

    override fun hashCode(): Int {
        return Objects.hash(id, uid, title, body)
    }
}

