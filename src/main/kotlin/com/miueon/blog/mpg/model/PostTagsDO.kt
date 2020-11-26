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
@TableName("post_tags")
@ApiModel(value = "PostTagsDO对象", description = "")
class PostTagsDO {

    @TableId(value = "id", type = IdType.AUTO)
    var id: Int? = null
    var pid: Int? = null
    var tid: Int? = null


    override fun toString(): String {
        return "PostTagsDO{" +
                "id=" + id +
                ", pid=" + pid +
                ", tid=" + tid +
                "}"
    }
}

