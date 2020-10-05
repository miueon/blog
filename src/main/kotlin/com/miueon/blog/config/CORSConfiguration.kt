package com.miueon.blog.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
import java.util.concurrent.Executors

@Configuration
class CORSConfiguration : WebMvcConfigurationSupport() {
    override fun addCorsMappings(rgistry: CorsRegistry) {
        rgistry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/")
        super.addResourceHandlers(registry)
    }
}