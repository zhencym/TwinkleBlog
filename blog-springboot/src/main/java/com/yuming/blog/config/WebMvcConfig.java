package com.yuming.blog.config;

import com.yuming.blog.handler.Interceptors.AccessInterceptor;
import com.yuming.blog.handler.Interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * mvc配置类
 *
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private AccessInterceptor accessInterceptor;

    /**
     * 添加拦截器,拦截顺序按照注册顺序
     * 路径拦截
     * 权限拦截
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加一个拦截器，拦截指定路径
        registry.addInterceptor(loginInterceptor)
            .addPathPatterns("/admin/*");
        // 只管后台的访问权限
        registry.addInterceptor(accessInterceptor)
            .addPathPatterns("/admin/*")
            .excludePathPatterns("/admin/user/menus");
    }

    /**
     * 解决跨域问题
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS");

        // 映射路径为/**，即所有路径都映射到这个跨域处理器上
        // 允许客户端发送cookie
        // 允许所有请求头
        // 允许所有源
        // 允许所有http方法
    }





}
