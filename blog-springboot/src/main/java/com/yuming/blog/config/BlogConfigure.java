package com.yuming.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: zhencym
 * @DATE: 2023/5/6
 * 其他bean的配置
 */
@Configuration
public class BlogConfigure {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

}
