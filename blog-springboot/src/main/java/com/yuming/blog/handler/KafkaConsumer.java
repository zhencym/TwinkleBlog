package cn.twinkle.lottery.application.mq.consumer;

import cn.hutool.core.lang.Assert;
import cn.twinkle.lottery.common.Constants;
import cn.twinkle.lottery.domain.activity.model.vo.InvoiceVO;
import cn.twinkle.lottery.domain.award.model.req.GoodsReq;
import cn.twinkle.lottery.domain.award.model.res.DistributionRes;
import cn.twinkle.lottery.domain.award.service.factory.DistributionGoodsFactory;
import cn.twinkle.lottery.domain.award.service.goods.IDistributionGoods;
import com.alibaba.fastjson.JSON;
import java.util.Optional;
import javax.annotation.Resource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @Author: zhencym
 * @DATE: 2023/4/30
 * 消息消费者
 */
@Component
public class LotteryInvoiceListener {

  private Logger logger = LoggerFactory.getLogger(LotteryInvoiceListener.class);

  @Resource
  private DistributionGoodsFactory distributionGoodsFactory;

  @KafkaListener(topics = "lottery_invoice", groupId = "lottery")
  public void topicTest(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    Optional<?> message = Optional.ofNullable(record.value());

    // 1. 判断消息是否存在
    if (!message.isPresent()) {
      return;
    }
    // 2. 处理 MQ 消息
    try{
      // 1、转化对象（以JSON格式反序列化）
      InvoiceVO invoiceVO = JSON.parseObject((String) message.get(), InvoiceVO.class);

      logger.info("消费者开始发奖 topic：{} uId：{} 发奖物品：{}", topic, invoiceVO.getUId(), JSON.toJSONString(invoiceVO));
      // 2. 获取发送奖品工厂，执行发奖
      // 当有重复消息的时候，会重复执行发奖；但是没事，我们利用uuid的唯一性，不管执行多次，能保证最终结果一致性即可（state_award = 1 表示成功颁奖的结果）
      IDistributionGoods distributionGoodsService = distributionGoodsFactory.getDistributionGoodsService(invoiceVO.getAwardType());
      DistributionRes distributionRes = distributionGoodsService.doDistribution(
          GoodsReq.builder()
              .uId(invoiceVO.getUId())
              .orderId(invoiceVO.getOrderId())
              .awardId(invoiceVO.getAwardId())
              .awardName(invoiceVO.getAwardName())
              .awardContent(invoiceVO.getAwardContent())
              .build());
      // Assert.isTrue(!Constants.AwardState.SUCCESS.getCode().equals(distributionRes.getCode()), distributionRes.getInfo());

      // 3. 打印日志
      logger.info("消费MQ消息，完成 topic：{} uId：{} 发奖结果：{}", topic, invoiceVO.getUId(), JSON.toJSONString(distributionRes));

      // 4. 消息消费完成,手动ack
      ack.acknowledge();
    } catch (Exception e) {
      // 发奖环节失败，消息重试。所有到环节，发货、更新库，都需要保证幂等。
      logger.error("消费MQ消息，失败 topic：{} message：{}", topic, message.get());

      // 暂时，错误也提交
//      ack.acknowledge();
      throw e;
    }
  }

}
