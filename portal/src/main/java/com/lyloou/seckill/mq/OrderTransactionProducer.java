package com.lyloou.seckill.mq;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;

/**
 * @author lilou
 * @since 2021/6/15
 */
@Component
public class OrderTransactionProducer {
    @Autowired
    private RocketMQProperties mqProperties;

    private TransactionMQProducer producer;

    @Autowired
    OrderTransactionProducerListener transactionListener;

    @Autowired
    @Qualifier("producerExecutor")
    ExecutorService producerExecutor;

    @PostConstruct
    public void init() {
        producer = new TransactionMQProducer(mqProperties.getProducer().getGroup());
        producer.setNamesrvAddr(mqProperties.getNameServer());
        producer.setSendMsgTimeout(Integer.MAX_VALUE);
        producer.setExecutorService(producerExecutor);
        producer.setTransactionListener(transactionListener);
        producer.setInstanceName("producer-order");
        this.start();
    }

    private void start() {
        try {
            this.producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public TransactionSendResult send(String data, String topic) throws MQClientException {
        Message message = new Message(topic, data.getBytes());
        return this.producer.sendMessageInTransaction(message, null);

    }

}
