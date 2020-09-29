package com.miueon.blog.config

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.cache.annotation.CachingConfigurerSupport
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.*
import java.time.Duration


@Configuration
class RedisConfig : CachingConfigurerSupport() {
    companion object{
        const val REDIS_KEY_DATABASE="miueonBlog"
    }


    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory)
            : RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String,Any>()
        val om = ObjectMapper()
                .registerModule(KotlinModule())
                .registerModule(JavaTimeModule())
                .activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Any::class.java)
                        .build(),ObjectMapper.DefaultTyping.EVERYTHING)
        val serializer = GenericJackson2JsonRedisSerializer(om)

        redisTemplate.setConnectionFactory(redisConnectionFactory)
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = serializer
        redisTemplate.hashKeySerializer = StringRedisSerializer()
        redisTemplate.hashValueSerializer =  serializer
        redisTemplate.afterPropertiesSet()
        return redisTemplate
    }

    @Bean
    fun redisSerializer(): RedisSerializer<Any> {
        // construct json serializer
        val serializer = FastJsonRedisSerializer(Any::class.java)
        val objectMapper = ObjectMapper()
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance ,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)

        return serializer
    }

    @Bean
    fun redisCacheManager(redisConnectionFactory: RedisConnectionFactory):
            RedisCacheManager {

        val redisCacheWriter = RedisCacheWriter
                .nonLockingRedisCacheWriter(redisConnectionFactory)
        // set the expire at one day
        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
        return RedisCacheManager(redisCacheWriter, redisCacheConfiguration)
    }
}