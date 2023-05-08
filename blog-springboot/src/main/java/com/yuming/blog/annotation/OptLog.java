package com.yuming.blog.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhencym
 * @DATE: 2023/5/6
 * 操作日志注解
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OptLog {


    /**
     * 注解参数，操作类型
     * @return
     */
    String optType() default "";

    /**
     * 操作模块
     * @return
     */
    String optModule() default "";

    /**
     * 操作说明
     * @return
     */
    String optDesc() default "";

}
