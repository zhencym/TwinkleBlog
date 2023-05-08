package com.yuming.blog.handler;

import com.alibaba.fastjson.JSON;
import com.yuming.blog.dto.EmailDTO;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * @Author: zhencym
 * @DATE: 2023/4/30
 * 消息生产者
 * 发送邮件
 */
@Component
public class KafkaProducer {

    private Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * MQ主题：邮件消息
     *
     */
    public static final String TOPIC_Blog_Email = "topic_blog_email";

    /**
     * 发送邮件
     *
     * @param emailDTO 发货单
     */
    public ListenableFuture<SendResult<String, Object>> sendBlogEmail(EmailDTO emailDTO) {
        String objJson = JSON.toJSONString(emailDTO);
        logger.info("发送MQ消息 topic：{}  message：{}", TOPIC_Blog_Email,  objJson);
        return kafkaTemplate.send(TOPIC_Blog_Email, objJson);
    }




}
