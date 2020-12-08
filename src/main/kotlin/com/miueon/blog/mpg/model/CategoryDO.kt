package com.miueon.blog.mpg.model

import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
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
@TableName("category")
@ApiModel(value = "CategoryDO对象", description = "")
class CategoryDO {

    @TableId(value = "id", type = IdType.AUTO)
    var id: Int? = null
    @TableField("c_name")
    var name: String? = null
    @TableField(exist = false)
    var postCount:Int = 0

    constructor()
    constructor(id: Int?, name: String){
        this.id = id
        this.name = name
    }
    companion object{
        val unclassified = CategoryDO(0, "unClassified")
    }

    override fun toString(): String {
        return "CategoryDO{" +
                "id=" + id +
                ", cName=" + name +
                "}"
    }
}

