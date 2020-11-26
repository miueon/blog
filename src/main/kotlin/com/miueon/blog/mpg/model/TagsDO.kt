package com.miueon.blog.mpg.model

import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * <p>
 *
 * </p>
 *
 * @author miueon
 * @since 2020-11-11
 */
@TableName("tags")
@ApiModel(value = "TagsDO对象", description = "")
class TagsDO {

    @TableId(value = "id", type = IdType.AUTO)
    var id: Int? = null
    var tName: String? = null


    override fun toString(): String {
        return "TagsDO{" +
                "id=" + id +
                ", tName=" + tName +
                "}"
    }
}

