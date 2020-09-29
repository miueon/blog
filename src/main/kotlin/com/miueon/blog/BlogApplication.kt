package com.miueon.blog

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@MapperScan("com.miueon.blog.mapper")

class BlogApplication

fun main(args: Array<String>) {
	runApplication<BlogApplication>(*args)
}
