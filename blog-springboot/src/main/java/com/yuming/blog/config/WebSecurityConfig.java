package com.yuming.blog.config;

import com.yuming.blog.handler.*;
import com.yuming.blog.handler.AccessDecisionManagerImpl;
import com.yuming.blog.handler.AccessDeniedHandlerImpl;
import com.yuming.blog.handler.AuthenticationEntryPointImpl;
import com.yuming.blog.handler.AuthenticationFailHandlerImpl;
import com.yuming.blog.handler.FilterInvocationSecurityMetadataSourceImpl;
import com.yuming.blog.handler.LogoutSuccessHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Security配置类
 *
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * 认证入口点
     */
    @Autowired
    private AuthenticationEntryPointImpl authenticationEntryPoint;
    /**
     * 拒绝访问处理
     */
    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;
    /**
     * 认证成功
     */
    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    /**
     * 认证失败
     */
    @Autowired
    private AuthenticationFailHandlerImpl authenticationFailHandler;
    /**
     * 退出处理类
     */
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /**
     * 储存应用中受保护的资源的安全元数据
     * @return
     */
    @Bean
    public FilterInvocationSecurityMetadataSource securityMetadataSource() {
        return new FilterInvocationSecurityMetadataSourceImpl();
    }

    /**
     * 决策用户是否有权限访问受保护资源
     * @return
     */
    @Bean
    public AccessDecisionManager accessDecisionManager() {
        return new AccessDecisionManagerImpl();
    }

    /**
     * 储存当前活跃的会话信息
     * @return
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * 用于在应用中发布会话
     * @return
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * 密码加密
     * 使用 PasswordEncoder.encode(password) 加密
     * 使用 PasswordEncoder.matches(inputpassword，encodepassword) 匹配密码是否正确
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    /**
     * 配置权限
     * 在springSecurity中使用配置时，主要就是是用这个方式configure实现各种配置
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 配置登录注销路径
        http.formLogin().loginProcessingUrl("/login").successHandler(authenticationSuccessHandler).failureHandler(authenticationFailHandler).and()
                .logout().logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler);
        // 配置路由权限信息
        http.authorizeRequests()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O fsi) {
                        fsi.setSecurityMetadataSource(securityMetadataSource());
                        fsi.setAccessDecisionManager(accessDecisionManager());
                        return fsi;
                    }
                })
                .anyRequest().permitAll()
                .and()
                //关闭跨站请求防护
                .csrf().disable().exceptionHandling()
                //未登录处理
                .authenticationEntryPoint(authenticationEntryPoint)
                //权限不足处理
                .accessDeniedHandler(accessDeniedHandler).and()
                //开启嵌入
                .headers().frameOptions().disable()
                .and()
                .sessionManagement()
                .maximumSessions(20)
                .sessionRegistry(sessionRegistry());
    }
    //忽略websocket拦截
    @Override
    public void configure(WebSecurity webSecurity){
        webSecurity.ignoring().antMatchers(
            "/ws/**"
        );
    }

}
