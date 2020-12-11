package com.miueon.blog.aop

import com.miueon.blog.service.RedisService
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.reflect.Method


/**
 * title: CacheTTLProcessorAspect

 * Author: Miueon

 * Date: 2020-12-08 3:29 p.m.

 * Version 1.0
 */
@Aspect
@Component
class CacheTTLProcessorAspect
{
    @Autowired
    lateinit var cacheService:RedisService

    @Around("@annotation(CacheTTL)")
    fun cacheTTL(joinPoint: ProceedingJoinPoint): Any {
        val method = getCurrentMethod(joinPoint)

        val parameters = joinPoint.args

        val key = CacheKeyGenerator.generateKey(method.name, parameters)

        var returnObject = cacheService[key]
        if (returnObject != null) {
             returnObject as List<Any>
            return returnObject.get(0)
        }

        returnObject = joinPoint.proceed(parameters)

        val cacheTTL = method.getAnnotation(CacheTTL::class.java)
        cacheService.sAdd(key,  cacheTTL.ttlMinutes.toLong(), returnObject)
        return returnObject
    }

    private fun getCurrentMethod(joinPoint: JoinPoint): Method {
        val signature = joinPoint.signature as MethodSignature
        return signature.method
    }
}

object CacheKeyGenerator {
    fun generateKey(methodName: String, vararg params: Any):String {
        if (params.isEmpty()) {
            return methodName.hashCode().toString()
        }
        val paramList = arrayOfNulls<Any>(params.size + 1)
        paramList[0] = methodName
        System.arraycopy(params, 0, paramList, 1, params.size)
        val hashCode: Int = paramList.contentDeepHashCode()
        return hashCode.toString()
    }
}