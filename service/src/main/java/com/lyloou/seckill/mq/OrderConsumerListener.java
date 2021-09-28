package com.lyloou.seckill.mq;

import cn.hutool.json.JSONUtil;
import com.lyloou.component.exceptionhandler.exception.BizException;
import com.lyloou.seckill.common.convertor.OrderConvertor;
import com.lyloou.seckill.common.dto.OrderDTO;
import com.lyloou.seckill.common.repository.entity.OrderEntity;
import com.lyloou.seckill.common.repository.entity.TransactionLogEntity;
import com.lyloou.seckill.common.repository.service.OrderService;
import com.lyloou.seckill.common.repository.service.TransactionLogService;
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
public class OrderConsumerListener implements MessageListenerConcurrently {
    @Autowired
    OrderService orderService;
    @Autowired
    OrderConvertor convertor;
    @Autowired
    TransactionLogService transactionLogService;

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
        final String transactionId = msg.getTransactionId();

        // 转换得到订单实体
        final OrderDTO order = JSONUtil.toBean(new String(msg.getBody()), OrderDTO.class);

        // 订单已经录入，不再重复录入
        if (orderService.lambdaQuery()
                .eq(OrderEntity::getOrderNo, order.getOrderNo())
                .count() > 0) {
            return;
        }

        if (msg.getReconsumeTimes() > 3) {
            log.warn("订单重复消息大于3次，可能下面的逻辑有问题，发送邮件给开发，msg:{}, order:{}", msg, order);
        }

        log.info("doMsg消息, OrderDTO：{}", order);

        // 创建订单
        final boolean saveOrderResult = orderService.save(convertor.convert(order));
        if (!saveOrderResult) {
            throw new BizException("保存订单失败, transactionId:" + transactionId);
        }

        // 写入事务日志
        transactionLogService.save(new TransactionLogEntity()
                .setId(transactionId)
                .setBusiness("order")
                .setForeignKey(String.valueOf(order.getId()))
        );

    }
}
