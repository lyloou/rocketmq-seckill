package com.lyloou.seckill.mq;

import cn.hutool.json.JSONUtil;
import com.google.common.base.Strings;
import com.lyloou.seckill.common.dto.PayResultDTO;
import com.lyloou.seckill.service.OrderManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lilou
 * @since 2021/6/11
 */
@Component
@Slf4j
public class PayConsumerListener implements MessageListenerConcurrently {
    @Autowired
    OrderManagerService orderManagerService;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        log.info("收到pay信息：{}", msgs);
        try {
            for (MessageExt msg : msgs) {
                doMsg(msg);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("处理消费者数据发生异常, msgs: " + msgs, e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    private void doMsg(MessageExt msg) {
        log.info("do pay信息：{}", msg);
        // 转换得到pay实体
        final PayResultDTO payResultDTO = JSONUtil.toBean(new String(msg.getBody()), PayResultDTO.class);
        if (payResultDTO == null) {
            log.warn("无效的消息: " + msg);
            return;
        }

        final String orderNo = payResultDTO.getOrderNo();
        if (Strings.isNullOrEmpty(orderNo)) {
            log.warn("无效的消息: " + msg);
            return;
        }
        orderManagerService.handle(payResultDTO);
    }
}
