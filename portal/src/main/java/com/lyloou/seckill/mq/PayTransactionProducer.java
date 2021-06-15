package com.lyloou.seckill.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
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
@Slf4j
public class PayTransactionProducer {
    @Autowired
    private RocketMQProperties mqProperties;

    private TransactionMQProducer producer;

    @Autowired
    PayTransactionProducerListener transactionListener;

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
        producer.setInstanceName("producer-pay");
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

    public void asyncSendWithDelayTimeLevel(String data, String topic, int delayTimeLevel)
            throws RemotingException, InterruptedException, MQClientException {
        Message message = new Message(topic, data.getBytes());
        message.setDelayTimeLevel(delayTimeLevel);
        this.producer.send(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("sendWithDelayAsync success, {}", sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.error("sendWithDelayAsync failed, ", e);
            }
        });
    }

}
