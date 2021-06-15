package com.lyloou.seckill.mq;

import cn.hutool.json.JSONUtil;
import com.lyloou.seckill.common.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lilou
 * @since 2021/6/11
 */
@Component
@Slf4j
public class OrderConsumerListener implements MessageListenerConcurrently {

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        log.info("收到订单信息：{}", msgs);
        try {
            for (MessageExt msg : msgs) {
                log.info("开始处理订单数据");
                doMsg(msg);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error("处理消费者数据发生异常, msgs: " + msgs, e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    private void doMsg(MessageExt msg) {
        // 转换得到订单实体
        final OrderDTO orderDTO = JSONUtil.toBean(new String(msg.getBody()), OrderDTO.class);

        log.info("doMsg消息, OrderDTO：{}", orderDTO);
    }
}
