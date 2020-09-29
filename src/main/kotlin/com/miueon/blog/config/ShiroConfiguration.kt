package com.miueon.blog.config

import com.miueon.blog.filter.JwtAuthFilter
import com.miueon.blog.filter.RestShiroFilterFactoryBean
import com.miueon.blog.realm.DbRealm
import com.miueon.blog.realm.JWTShiroRealm


import com.miueon.blog.service.UserService
import org.apache.shiro.authc.Authenticator
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy
import org.apache.shiro.authc.pam.ModularRealmAuthenticator
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.mgt.SessionStorageEvaluator
import org.apache.shiro.realm.Realm
import org.apache.shiro.spring.web.ShiroFilterFactoryBean
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition
import org.apache.shiro.web.mgt.DefaultWebSessionStorageEvaluator
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.servlet.DispatcherType
import javax.servlet.Filter

@Configuration
class ShiroConfiguration{

    /**
     * 注册shiro的Filter，拦截请求
     */
    @Bean
    fun filterRegistrationBean(securityManager: SecurityManager, userService: UserService):
            FilterRegistrationBean<Filter> {
        val filterRegistrationBean = FilterRegistrationBean<Filter>()
        filterRegistrationBean.filter = shiroFilter(securityManager, userService).`object` as Filter
        filterRegistrationBean.addInitParameter("targetFilterLifecycle", "true")
        filterRegistrationBean.isAsyncSupported = true
        filterRegistrationBean.isEnabled = true
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC)
        return filterRegistrationBean
    }

    /**
     * 设置过滤器 将自定义的Filter加入
     */
    @Bean
    fun shiroFilter(securityManager: SecurityManager, userService: UserService)
            : ShiroFilterFactoryBean {

        val factoryBean = RestShiroFilterFactoryBean()

        factoryBean.securityManager = securityManager
        val filterMap:MutableMap<String, Filter> = factoryBean.filters
        filterMap["authcToken"] = createAuthFilter((userService))
       // TODO("add anRole with filter")
        factoryBean.filters = filterMap
        factoryBean.filterChainDefinitionMap = shiroFilterChainDefinition().filterChainMap
        return factoryBean
    }

    @Bean
    fun shiroFilterChainDefinition(): ShiroFilterChainDefinition {
        val chainDefinition = DefaultShiroFilterChainDefinition()
        chainDefinition.addPathDefinition("/auth/login", "noSessionCreation,anon")
        chainDefinition.addPathDefinition("/logout", "noSessionCreation," +
                "authcToken[permissive]")
      //  chainDefinition.addPathDefinition("/image/**", "anon")

        //只允许admin或manager角色的用户访问

        // 这里只是设置definition, 后面filter通过definition在filterMap中查找对于字符串匹配的filter
        // e.g. 上面的authcToken和这里用,分割的子串
        // anyRole也是对应上面的

        //只允许admin或manager角色的用户访问

        // 这里只是设置definition, 后面filter通过definition在filterMap中查找对于字符串匹配的filter
        // e.g. 上面的authcToken和这里用,分割的子串
        // anyRole也是对应上面的

//        chainDefinition.addPathDefinition("/admin/**",
//                "noSessionCreation,authcToken,anyRole[admin,manager]")
//
//        chainDefinition.addPathDefinition("/article/list", "noSessionCreation,authcToken")
//        chainDefinition.addPathDefinition("/article/*",
//                "noSessionCreation,authcToken[permissive]")

        // TODO: add role support

        chainDefinition.addPathDefinition("/auth/**", "noSessionCreation,authcToken")
        chainDefinition.addPathDefinition("/api/posts/**", "noSessionCreation,authcToken")
        chainDefinition.addPathDefinition("/api/comments/**", "noSessionCreation,authcToken")

       // chainDefinition.addPathDefinition("/**", "noSessionCreation,authcToken")

        return chainDefinition
    }

    @Bean("dbRealm")
    fun dbShiroRealm(userService: UserService): Realm {
        return DbRealm(userService)
    }

    @Bean("jwtRealm")
    fun jwtShiroRealm(userService: UserService): Realm {
        return JWTShiroRealm(userService)
    }

    /**
     * 初始化Authenticator
     */
    @Bean
    fun authenticator(userService: UserService): Authenticator {
        val authentiactor = ModularRealmAuthenticator()
        authentiactor.setRealms(listOf(dbShiroRealm(userService), jwtShiroRealm(userService)))
        authentiactor.authenticationStrategy = FirstSuccessfulStrategy()
        return authentiactor
    }

    /**
     * 禁用session, 不保存用户登录状态。保证每次请求都重新认证。
     * 需要注意的是，如果用户代码里调用Subject.getSession()
     * 还是可以用session，如果要完全禁用，要配合下面的noSessionCreation的Filter来实现
     */
    @Bean
    protected fun sessionStorageEvaluator(): SessionStorageEvaluator? {
        val sessionStorageEvaluator = DefaultWebSessionStorageEvaluator()
        sessionStorageEvaluator.isSessionStorageEnabled = false
        return sessionStorageEvaluator
    }

    /* Utils */
    fun createAuthFilter(userService: UserService): JwtAuthFilter {
        return JwtAuthFilter(userService)
    }
}