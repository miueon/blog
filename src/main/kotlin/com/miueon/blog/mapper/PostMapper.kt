package com.miueon.blog.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.pojo.post

interface PostMapper : BaseMapper<post> {
    fun selectByIDs(page: Page<post>, idList: List<Long>):Page<post>
}