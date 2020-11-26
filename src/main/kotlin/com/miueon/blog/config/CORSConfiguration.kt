package com.miueon.blog.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import org.springframework.web.servlet.config.annotation.*
import java.util.concurrent.Executors

@Configuration
class CORSConfiguration : WebMvcConfigurer {
    override fun addCorsMappings(rgistry: CorsRegistry) {
        rgistry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // to solve static access
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/")
        // 解决 SWAGGER 404报错
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/")

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
    }
}