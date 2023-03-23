package com.yuming.blog.handler;

import com.alibaba.fastjson.JSON;
import com.yuming.blog.dto.EmailDTO;
import com.yuming.blog.constant.MQPrefixConst;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 通知邮箱
 * MQ消费者，消费EMAIL_QUEUE邮件消息队列，实现发送通知邮件
 *
 **/
@Component
@RabbitListener(queues = MQPrefixConst.EMAIL_QUEUE)
public class EmailReceiver {

    /**
     * 邮箱号
     */
    @Value("${spring.mail.username}")
    private String email;

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * 监听到消息队列有消息，就执行以下操作
     * @param data
     */
    @RabbitHandler
    public void process(byte[] data) {
        //json格式的邮件数据，转为EmailDTO
        EmailDTO emailDTO = JSON.parseObject(new String(data), EmailDTO.class);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(email); //发送人
        message.setTo(emailDTO.getEmail()); //接收者
        message.setSubject(emailDTO.getSubject()); //主题
        message.setText(emailDTO.getContent()); //正文
        javaMailSender.send(message);
    }

}
