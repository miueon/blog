package com.miueon.blog.mpg

import com.baomidou.mybatisplus.generator.AutoGenerator
import com.baomidou.mybatisplus.generator.config.*
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine

fun main() {
    // 表名，多个继续写
    val tableNameList = listOf(
            "authority", "category",
            "comment", "post", "post_tags",
            "tags", "user", "user_authority"
    )

    val mpg = AutoGenerator()
    // 全局配置
    val gc = GlobalConfig()
    val projectPath = System.getProperty("user.dir")
    gc.outputDir = "$projectPath/src/main/kotlin"
    gc.author = "miueon"
    gc.isOpen = false
    gc.isKotlin = true
    gc.isSwagger2 = true
    gc.entityName = "%sDO"
    mpg.globalConfig = gc

    // 数据源配置
    val dsc = DataSourceConfig()
    dsc.url = "jdbc:mysql://localhost:3306/blog?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=true"
    dsc.driverName = "com.mysql.jdbc.Driver"
    dsc.username = "root"
    dsc.password = "123456"
    mpg.dataSource = dsc

    // 包配置
    val pc = PackageConfig()
    // 父包名，自行修改
    pc.parent = "com.miueon.blog.mpg"
    pc.entity = "model"
    pc.service = "service"
    pc.mapper = "mapper"
    mpg.packageInfo = pc


    // 配置模板
    val templateConfig = TemplateConfig()

    // 配置自定义输出模板
    templateConfig.entityKt = "mpgTemplates/entity.kt"
    templateConfig.mapper = "mpgTemplates/mapper.kt"
//    templateConfig.service = "mpgTemplates/service.kt"
//    templateConfig.serviceImpl = "templates/serviceImpl.kt"

    templateConfig.controller = null
    templateConfig.xml = null
    templateConfig.service = null
    templateConfig.serviceImpl = null
    mpg.template = templateConfig

    // 策略配置
    val strategy = StrategyConfig()
    strategy.naming = NamingStrategy.underline_to_camel
    strategy.columnNaming = NamingStrategy.underline_to_camel
    strategy.superEntityClass = "com.mybatis.app.common.BaseEntity"


    // 写于父类中的公共字段
    strategy.setSuperEntityColumns("auto_id")
    strategy.setInclude(*tableNameList.toTypedArray())
    strategy.isControllerMappingHyphenStyle = true
    strategy.setTablePrefix(pc.moduleName + "_")
    mpg.strategy = strategy
    mpg.templateEngine = VelocityTemplateEngine()
    mpg.execute()

}