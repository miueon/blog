package com.miueon.blog.aop

import org.springframework.stereotype.Component
import java.lang.annotation.RetentionPolicy


@Component
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)

annotation class CacheTTL(val value:String = "",
                          val cacheName:String = "",
                          val ttlMinutes:Int = 1
                          )
