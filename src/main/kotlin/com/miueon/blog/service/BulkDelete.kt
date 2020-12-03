package com.miueon.blog.service

import com.miueon.blog.pojo.IdList
import com.miueon.blog.util.ApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.lang.RuntimeException

interface BulkDelete {
    fun prepToDelete(idList: IdList, key: DELETEKEY)
    fun getDeleteInfo(k: DELETEKEY): Set<Int>
}

enum class DELETEKEY {
    CATEGORY,
    POST,
    TAG
}

@Service
class BulkDeleteImpl : BulkDelete {
    @Autowired
    lateinit var redisService: RedisService

    private fun getDeleteKey(key: DELETEKEY): String {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.name + key
    }

    override fun prepToDelete(idList: IdList, k: DELETEKEY) {
        try {
            val key = getDeleteKey(k)
            if (redisService.sMembers(key) != null) {
                redisService.del(key)
            }
            redisService.sAdd(key, 3600, *idList.toTypedArray())
        } catch (e: RuntimeException) {
            throw ApiException(e, HttpStatus.BAD_REQUEST)
        }
    }

    override fun getDeleteInfo(k: DELETEKEY): Set<Int> {
        val key = getDeleteKey(k)
        val ids: Set<Int> = redisService.sMembers(key) as Set<Int>?
                ?: throw ApiException("no object to be deleted.")
        if (ids.isEmpty()) {
            throw ApiException("no object to be deleted.")
        }
        return ids

    }
}