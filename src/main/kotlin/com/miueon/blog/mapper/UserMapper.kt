package com.miueon.blog.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.miueon.blog.pojo.user

interface UserMapper: BaseMapper<user> {
    fun selectAll():List<user>
}