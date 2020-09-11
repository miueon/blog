package com.miueon.blog.config

import com.miueon.blog.filter.AuthHeader
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.lang.Nullable
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice


@ControllerAdvice
class ResponseHeaderAdvice : ResponseBodyAdvice<Any> {
    override fun supports(p0: MethodParameter, p1: Class<out HttpMessageConverter<*>>): Boolean {
        return true
    }

    override fun beforeBodyWrite(@Nullable p0: Any?, methodParameter: MethodParameter,
                                 mediaType: MediaType, p3: Class<out HttpMessageConverter<*>>,
                                 serverHttpRequest: ServerHttpRequest,
                                 serverHttpResponse: ServerHttpResponse): Any? {

        val serverRequest = serverHttpRequest as ServletServerHttpRequest
        val serverResponse = serverHttpResponse as ServletServerHttpResponse

        // 对于未添加跨域消息头的响应进行处理
        val request = serverRequest.servletRequest
        val response = serverResponse.servletResponse
        val originHeader = "Access-Control-Allow-Origin"
        if (!response.containsHeader(originHeader)) {
            var origin = request.getHeader("Origin")
            if (origin == null) {
                val referer = request.getHeader("Referer")
                if (referer != null) origin = referer.substring(0, referer.indexOf("/", 7))
            }
            response.setHeader("Access-Control-Allow-Origin", origin)
        }
        val allowHeaders = "Access-Control-Allow-Headers"
        if (!response.containsHeader(allowHeaders)) response.setHeader(allowHeaders,
                request.getHeader(allowHeaders))
        val allowMethods = "Access-Control-Allow-Methods"
        if (!response.containsHeader(allowMethods)) response.setHeader(allowMethods, "GET,POST,OPTIONS,HEAD")
        // 上面的都是boilerplate
        //这个很关键，要不然ajax调用时浏览器默认不会把这个token的头属性返给JS
        val exposeHeaders = "access-control-expose-headers"
        if (!response.containsHeader(exposeHeaders)) response.setHeader(exposeHeaders, AuthHeader)
        return p0
    }


}
