package com.yuming.blog.config;

import com.yuming.blog.constant.MQPrefixConst;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Rabbitmq配置类
 *
 */
@Configuration
public class RabbitConfig {

    /**
     * 新建队列 名字为 MAXWELL_QUEUE=article
     * 1、name:    队列名称
     * 2、durable: 是否持久化
     * 3、exclusive: 是否独享、排外的。如果设置为true，定义为排他队列。则只有创建者可以使用此队列。也就是private私有的。
     * 4、autoDelete: 是否自动删除。也就是临时队列。当最后一个消费者断开连接后，会自动删除。
     * @return
     */

    //maxwell队列主要用于es进行信息传递
    @Bean
    public Queue articleQueue() {
        return new Queue(MQPrefixConst.MAXWELL_QUEUE, true);
    }

    /**
     * 新建交换机 名字为 MAXWELL_EXCHANGE=maxwell
     * @return
     */
    @Bean
    public FanoutExchange maxWellExchange() {
        //Fanout交换机
        return new FanoutExchange(MQPrefixConst.MAXWELL_EXCHANGE, true, false);
    }

    /**
     * 绑定交换机和队列
     * @return
     */
    @Bean
    public Binding bindingArticleDirect() {
        //链式写法，绑定交换机和队列，并设置匹配键
        return BindingBuilder
            //绑定队列
            .bind(articleQueue())
            //到交换机
            .to(maxWellExchange());
    }



    /**
     * 新建队列 名字为 EMAIL_QUEUE=email
     * @return
     */
    //email队列主要是用于发送邮件的消息队列。
    @Bean
    public Queue emailQueue() {
        return new Queue(MQPrefixConst.EMAIL_QUEUE, true);
    }

    /**
     * 新建交换机 名字为 EMAIL_EXCHANGE=send
     * @return
     */
    @Bean
    public FanoutExchange emailExchange() {
        return new FanoutExchange(MQPrefixConst.EMAIL_EXCHANGE, true, false);
    }

    /**
     * 绑定交换机和队列
     * @return
     */
    @Bean
    public Binding bindingEmailDirect() {
        return BindingBuilder.
            bind(emailQueue()).
            to(emailExchange());
    }

}
