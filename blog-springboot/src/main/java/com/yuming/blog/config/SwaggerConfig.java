package com.yuming.blog.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

/**
 * swagger配置类
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .protocols(Collections.singleton("https"))
                .host("192.168.0.106:8088") //前端请求到的后端地址
                .apiInfo(apiInfo()) //配置api信息
                .groupName("小育")  //指定分组名。有多个docker就有多个分组就有多个名字
                .enable(true)//eanble决定了是否启动swagger，发布时记得关掉
                .select()  //指定我们需要基于什么包扫描
                .apis(RequestHandlerSelectors.basePackage("com.minzheng.blog.controller")) //指定接口所在的包
                //apis:
                //RequestHandlerSelectors扫描接口的方式
                //basePackage指定扫描包
                //any（）扫描全部
                //none（）不扫描
                //withclassannotation 扫描类的注解（里面必须放注解的反射对象）
                .paths(PathSelectors.any())
                //path：过滤哪里什么路径
                .build();

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("博客api文档") //文档标题
                .description("springboot+vue开发的博客项目") //文档简介
                .termsOfServiceUrl("http://192.168.0.106:8084/")//前端地址
                .version("1.0")
                .build();
    }
}
