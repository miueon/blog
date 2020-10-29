package com.miueon.blog.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.miueon.blog.pojo.user
import org.springframework.stereotype.Repository

@Repository
interface UserMapper: BaseMapper<user> {
    fun selectAll():List<user>
}