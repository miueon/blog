package com.miueon.blog.util

import com.baomidou.mybatisplus.extension.plugins.pagination.Page

class Page4Navigator<T> {
    var content:MutableList<T>? = null
    var isHasContent:Boolean = false
    var isHasNext = false
    var isHasPrevious = false
    var totalPages:Long = 0
    var navigatePages:Int = 0
    var navigatePageNums:LongArray? = null
    var current: Long = 0
    constructor()
    constructor(page: Page<T>, navigatePages: Int){
        content = page.records
        content?.let {
            isHasContent = true
        }
        totalPages = page.pages
        isHasNext = page.hasNext()
        isHasPrevious = page.hasPrevious()
        this.navigatePages = navigatePages
        current = page.current
        calcNavigatePageNums()
    }

    // todo: write a test
    fun calcNavigatePageNums() {
        val nums:MutableList<Long> = ArrayList()
        if (totalPages <= navigatePages) {
            for (i in 1..totalPages) {
                nums.add(i)
            }
        } else {
            var startNum = current - navigatePages/2
            val endNum = current + navigatePages/2
            when {
                startNum < 1 -> {
                    for (i in 1..navigatePages) {
                        nums.add(i.toLong())
                    }
                }
                endNum > totalPages -> {
                    startNum = totalPages + 1 - navigatePages
                    for (i in 0 until navigatePages) {
                        nums.add(startNum++)
                    }
                }
                else -> {
                    for (i in 0 until navigatePages) {
                        nums.add(startNum++)
                    }
                }
            }
        }
        this.navigatePageNums = nums.toLongArray()
    }

}