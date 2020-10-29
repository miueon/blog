package com.miueon.blog.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.miueon.blog.pojo.comment
import org.springframework.stereotype.Repository

@Repository
interface CommentMapper: BaseMapper<comment> {

}