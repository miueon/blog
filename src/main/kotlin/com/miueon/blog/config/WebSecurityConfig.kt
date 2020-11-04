package com.miueon.blog.config

import com.miueon.blog.filter.OptionRequestFilter
import com.miueon.blog.security.*
import com.miueon.blog.service.JwtAuthenticationProvider
import com.miueon.blog.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.header.Header
import org.springframework.security.web.header.writers.StaticHeadersWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import java.util.*

@EnableWebSecurity
class WebSecurityConfig(
        @Autowired val userService: UserService):WebSecurityConfigurerAdapter() {
    @Bean("jwtAuthenticationProvider")
    fun jwtAuthenticationProvider(): AuthenticationProvider {
        return JwtAuthenticationProvider(userService)
    }
    @Bean("daoAuthenticationProvider")
    fun daoAuthenticationProvider(): AuthenticationProvider {
        val daoProvider = DaoAuthenticationProvider()
        daoProvider.setUserDetailsService(userService)
        return daoProvider
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                .antMatchers("/auth/user/**").hasRole("USER")
             //   .antMatchers("/admin/api/**").hasRole("ADMIN")
//                .antMatchers(AUTH_WHITELIST).permitAll()
//                .antMatchers(HttpMethod.POST, "/cachedemo/v1/users/signup").permitAll()
//                .anyRequest().authenticated()
                .anyRequest().permitAll()
                .and()
                .csrf().disable()
                .formLogin().disable()
                .sessionManagement().disable()
                .cors()
                .and()
                .headers()
                // the default return headers would not contain item other than
                // [ cache-control, content-type ]
                .addHeaderWriter(StaticHeadersWriter(
                        listOf(Header("Access-control-Allow-Origin", "*"),
                                Header("Access-Control-Expose-Headers",
                                        "Authorization"))
                ))
                .and() // the options request will directly return the header
                .addFilterAfter(OptionRequestFilter(), CorsFilter::class.java)
                .apply(JsonLoginConfigurer()).loginSuccessHandler(jsonLoginSuccessHandler()) // custom dsl
                .and()
                .apply(JwtLoginConfigurer()).tokenValidSuccessHandler(jwtRefreshSuccessHandler())
                .permissiveRequestUrls("/logout")
                .and()
                .logout()
                .addLogoutHandler(tokenClearLogoutHandler())
                .logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler())
                .logoutSuccessUrl("/")
                .and()
                .sessionManagement().disable()
    }

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth?.authenticationProvider(daoAuthenticationProvider())
                ?.authenticationProvider(jwtAuthenticationProvider())
    }

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager? {
        return super.authenticationManagerBean()
    }

    @Bean
    protected fun jsonLoginSuccessHandler(): JsonLoginSuccessHandler {
        return JsonLoginSuccessHandler(userService)
    }

    @Bean
    protected fun jwtRefreshSuccessHandler(): JwtRefreshSuccessHandler {
        return JwtRefreshSuccessHandler(userService)
    }

    @Bean
    protected fun tokenClearLogoutHandler(): TokenClearLogoutHandler {
        return TokenClearLogoutHandler(userService)
    }

    @Bean
    protected fun corsConfigurationSource(): CorsConfigurationSource? {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = Arrays.asList("*")
        configuration.allowedMethods = Arrays.asList("GET", "POST", "HEAD", "OPTION")
        configuration.allowedHeaders = Arrays.asList("*")
        configuration.addExposedHeader("Authorization")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

}