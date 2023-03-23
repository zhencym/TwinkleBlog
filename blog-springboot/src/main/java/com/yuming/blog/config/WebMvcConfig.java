package com.yuming.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * mvc配置类
 *
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 静态资源处理
     * 自定义静态资源映射目录，当访问swagger-ui.html资源时，
     * 资源处理器告诉spring从classpath:/META-INF/resources/找到指定资源返回
     * addResoureHandler：指的是对外暴露的访问路径
     * addResourceLocations：指的是内部文件放置的目录
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html") //访问路径
                .addResourceLocations("classpath:/META-INF/resources/"); //本地路径
    }

    /**
     * 解决跨域问题
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许跨域访问的路径
                .allowCredentials(true)  // 是否发送cookie
                .allowedHeaders("*") // 允许头部设置
                .allowedOriginPatterns("*") // 允许跨域访问的源
                .allowedMethods("*"); // 允许请求方法
        // 映射路径为/**，即所有路径都映射到这个跨域处理器上
        // 允许客户端发送cookie
        // 允许所有请求头
        // 允许所有源
        // 允许所有http方法
    }

}
