package com.miueon.blog.mpg.model

import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.miueon.blog.mpg.CommentUserInfo
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * <p>
 *
 * </p>
 *
 * @author miueon
 * @since 2020-11-11
 */
@TableName("comment")
@ApiModel(value = "CommentDO对象", description = "")
class CommentDO {

    @TableId(value = "id", type = IdType.AUTO)
    var id: Int? = null
    var uid: Int? = null
    var pid: Int? = null

    @TableField("createdDate")
    var createdDate: LocalDateTime = LocalDateTime.now(ZoneId.of("+08:00"))
    var content: String? = null

    @TableField(exist = false)
    var usr:CommentUserInfo? = null

    override fun toString(): String {
        return "CommentDO{" +
                "id=" + id +
                ", uid=" + uid +
                ", pid=" + pid +
                ", createdDate=" + createdDate +
                ", content=" + content +
                "}"
    }
}

