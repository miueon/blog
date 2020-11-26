package com.miueon.blog.service

import com.miueon.blog.mpg.model.TagsDO
import org.springframework.stereotype.Service

interface TagPostService {
    fun getTagListByPostId(pid: Int):List<TagsDO>
    fun savePostTagRel(pid: Int, tagIdList: List<Int>)
    fun deleteByPid(pid:Int)
    fun deleteByTid(tid:Int)
}
@Service
class TagPostServiceImpl : TagPostService {
    override fun getTagListByPostId(pid: Int): List<TagsDO> {
        TODO("Not yet implemented")
    }

    override fun savePostTagRel(pid: Int, tagIdList: List<Int>) {
        TODO("Not yet implemented")
    }

    override fun deleteByPid(pid: Int) {
        TODO("Not yet implemented")
    }

    override fun deleteByTid(tid: Int) {
        TODO("Not yet implemented")
    }
}