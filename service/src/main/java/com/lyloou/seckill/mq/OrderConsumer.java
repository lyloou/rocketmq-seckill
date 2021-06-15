package com.lyloou.seckill.mq;

import com.lyloou.seckill.common.dto.Constant;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author lilou
 * @since 2021/6/15
 */
@Component
public class OrderConsumer {
    DefaultMQPushConsumer consumer;

    @Autowired
    OrderConsumerListener listener;

    @Autowired
    RocketMQProperties mqProperties;

    @PostConstruct
    public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer(Constant.GROUP_ORDER);
        consumer.setNamesrvAddr(mqProperties.getNameServer());
        consumer.subscribe(Constant.TOPIC_ORDER, "*");
        consumer.registerMessageListener(listener);
        consumer.start();
    }
}
