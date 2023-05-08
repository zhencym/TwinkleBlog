package com.yuming.blog.handler;

import com.alibaba.fastjson.JSON;
import com.yuming.blog.dto.EmailDTO;
import java.util.Optional;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @Author: zhencym
 * @DATE: 2023/4/30
 * 消息消费者
 */
@Component
public class KafkaConsumer {

  private Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
  /**
   * 邮箱号
   */
  @Value("${spring.mail.username}")
  private String email;

  @Autowired
  private JavaMailSender javaMailSender;



  @KafkaListener(topics = "topic_blog_email", groupId = "blog")
  public void topicTest(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

    // 1. 判断消息是否存在
    Optional<?> message = Optional.ofNullable(record.value());
    if (!message.isPresent()) {
      return;
    }
    // 2. 处理 MQ 消息
    try{
      //json格式的邮件数据，转为EmailDTO
      EmailDTO emailDTO = JSON.parseObject((String)message.get(), EmailDTO.class);
      SimpleMailMessage msg = new SimpleMailMessage();
      msg.setFrom(email);
      msg.setTo(emailDTO.getEmail());
      msg.setSubject(emailDTO.getSubject());
      msg.setText(emailDTO.getContent());
      javaMailSender.send(msg);
      // System.err.println("消费");
      logger.info("Email消息消费，完成 topic：{} 邮件消息：{}", topic,  JSON.toJSONString(msg));
      // 3. 消息消费完成,手动ack
      ack.acknowledge();
    } catch (Exception e) {
      // 消息消费失败，抛异常会自动消息重试。所有到环节，发货、更新库，都需要保证幂等。
      logger.error("Email消息消费，失败 topic：{} message：{}", topic, message.get());
      throw e;
    }
  }

}
