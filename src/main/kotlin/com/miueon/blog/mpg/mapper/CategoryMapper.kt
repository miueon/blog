package com.miueon.blog.mpg.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.miueon.blog.mpg.model.CategoryDO
import org.springframework.stereotype.Repository

@Repository
/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author miueon
 * @since 2020-11-11
 */
interface CategoryMapper : BaseMapper<CategoryDO>{
    fun deleteByPrimaryKey(id: Int?): Int

    override fun insert(record: CategoryDO?): Int

    fun insertSelective(record: CategoryDO?): Int

    fun selectByPrimaryKey(id: Int?): CategoryDO?

    fun updateByPrimaryKeySelective(record: CategoryDO?): Int

    fun updateByPrimaryKey(record: CategoryDO?): Int
}


