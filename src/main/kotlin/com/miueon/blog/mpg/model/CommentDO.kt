package com.miueon.blog.mpg.model

import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime

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
    var createdDate: LocalDateTime = LocalDateTime.now()
    var content: String? = null


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

