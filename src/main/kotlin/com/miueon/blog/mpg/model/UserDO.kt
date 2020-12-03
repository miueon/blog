package com.miueon.blog.mpg.model

import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
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
@TableName("user")
@ApiModel(value = "UserDO对象", description = "")
class UserDO {

    @TableId(value = "id", type = IdType.AUTO)
    var id: Int? = null
    var email: String? = null
    var name: String? = null
    var password: String? = null
    var salt: String? = null
    var aid: Int? = null
    var url:String? =null
    @TableField(exist = false)
    var role: String? = null

    @TableField("createdDate")
    var createdDate: LocalDateTime = LocalDateTime.now(ZoneId.of("+08:00"))


    override fun toString(): String {
        return "UserDO{" +
                "id=" + id +
                ", email=" + email +
                ", name=" + name +
                ", password=" + password +
                ", salt=" + salt +
                ", createdDate=" + createdDate +
                "}"
    }
}

