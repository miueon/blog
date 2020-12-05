package com.miueon.blog.mpg.model

import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.miueon.blog.mpg.CommentUserInfo
import com.miueon.blog.validator.Insert
import com.miueon.blog.validator.Update
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime
import java.time.ZoneId
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

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
class CommentDO(var pid: Int? = null,
                var content: String? = null,
                @TableField(exist = false)
                var usr: CommentUserInfo? = null
) {

    @TableId(value = "id", type = IdType.AUTO)
    var id: Int? = null
    var uid: Int? = null
    @TableField("createdDate")
    var createdDate: LocalDateTime = LocalDateTime.now(ZoneId.of("+08:00"))

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

