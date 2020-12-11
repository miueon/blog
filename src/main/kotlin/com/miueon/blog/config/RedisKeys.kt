package com.miueon.blog.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * title: RedisKeys

 * Author: Miueon

 * Date: 2020-12-11 5:53 p.m.

 * Version 1.0
 */
@Configuration
@ConfigurationProperties("spring.redis.keys")
class RedisKeys {
    lateinit var archiveKey:String
    lateinit var latestPost:String
}