package com.miueon.blog.service

interface RedisService {

    /**
     * 保存属性
     */
    operator fun set(key: String?, value: Any?, time: Long)

    /**
     * 保存属性
     */
    operator fun set(key: String?, value: Any?)

    /**
     * 获取属性
     */
    operator fun get(key: String?): Any?

    /**
     * 删除属性
     */
    fun del(key: String?): Boolean?

    /**
     * 批量删除属性
     */
    fun del(keys: List<String?>?): Long?

    /**
     * 设置过期时间
     */
    fun expire(key: String?, time: Long): Boolean?

    /**
     * 获取过期时间
     */
    fun getExpire(key: String?): Long?

    /**
     * 判断是否有该属性
     */
    fun hasKey(key: String?): Boolean

    /**
     * 按delta递增
     */
    fun incr(key: String?, delta: Long): Long?

    /**
     * 按delta递减
     */
    fun decr(key: String?, delta: Long): Long?

    /**
     * 获取Hash结构中的属性
     */
    fun hGet(key: String?, hashKey: String?): Any?

    /**
     * 向Hash结构中放入一个属性
     */
    fun hSet(key: String?, hashKey: String?, value: Any?, time: Long): Boolean?

    /**
     * 向Hash结构中放入一个属性
     */
    fun hSet(key: String?, hashKey: String?, value: Any?)

    /**
     * 直接获取整个Hash结构
     */
    fun hGetAll(key: String?): Map<Any?, Any?>?

    /**
     * 直接设置整个Hash结构
     */
    fun hSetAll(key: String?, map: Map<String?, Any?>?, time: Long): Boolean?

    /**
     * 直接设置整个Hash结构
     */
    fun hSetAll(key: String?, map: Map<String?, Any?>?)

    /**
     * 删除Hash结构中的属性
     */
    fun hDel(key: String?, vararg hashKey: Any?)

    /**
     * 判断Hash结构中是否有该属性
     */
    fun hHasKey(key: String?, hashKey: String?): Boolean?

    /**
     * Hash结构中属性递增
     */
    fun hIncr(key: String?, hashKey: String?, delta: Long?): Long?

    /**
     * Hash结构中属性递减
     */
    fun hDecr(key: String?, hashKey: String?, delta: Long?): Long?

    /**
     * 获取Set结构
     */
    fun sMembers(key: String?): Set<Any?>?

    /**
     * 向Set结构中添加属性
     */
    fun sAdd(key: String?, vararg values: Any?): Long?

    /**
     * 向Set结构中添加属性
     */
    fun sAdd(key: String?, time: Long, vararg values: Any?): Long?

    /**
     * 是否为Set中的属性
     */
    fun sIsMember(key: String?, value: Any?): Boolean?

    /**
     * 获取Set结构的长度
     */
    fun sSize(key: String?): Long?

    /**
     * 删除Set结构中的属性
     */
    fun sRemove(key: String?, vararg values: Any?): Long?

    /**
     * 获取List结构中的属性
     */
    fun lRange(key: String?, start: Long, end: Long): List<Any?>?

    /**
     * 获取List结构的长度
     */
    fun lSize(key: String?): Long?

    /**
     * 根据索引获取List中的属性
     */
    fun lIndex(key: String?, index: Long): Any?

    /**
     * 向List结构中添加属性
     */
    fun lrPush(key: String?, value: Any?): Long?
    fun llPush(key: String?, value: Any?):Long?
    /**
     * 向List结构中添加属性
     */
    fun lrPush(key: String?, value: Any?, time: Long): Long?

    /**
     * 向List结构中批量添加属性
     */
    fun lrPushAll(key: String?, vararg values: Any?): Long?

    /**
     * 向List结构中批量添加属性
     */
    fun lrPushAll(key: String?, time: Long?, vararg values: Any?): Long?

    /**
     * 从List结构中移除属性
     */
    fun lRemove(key: String?, count: Long, value: Any?): Long?

    fun lrPop(key: String?):Any?
}