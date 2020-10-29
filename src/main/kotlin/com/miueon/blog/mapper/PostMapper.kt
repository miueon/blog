package com.miueon.blog.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.pojo.post
import org.springframework.stereotype.Repository

@Repository
interface PostMapper : BaseMapper<post> {
    fun selectByIDs(page: Page<post>, idList: List<Long>):Page<post>
}