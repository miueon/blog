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
@TableName("user_authority")
@ApiModel(value = "UserAuthorityDO对象", description = "")
class UserAuthorityDO {

    @TableId(value = "id", type = IdType.AUTO)
    var id: Int? = null
    var uid: Int? = null
    var aid: Int? = null


    override fun toString(): String {
        return "UserAuthorityDO{" +
                "id=" + id +
                ", uid=" + uid +
                ", aid=" + aid +
                "}"
    }
}

