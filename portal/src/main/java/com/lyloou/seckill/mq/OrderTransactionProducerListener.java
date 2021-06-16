package com.lyloou.seckill.mq;

import cn.hutool.json.JSONUtil;
import com.lyloou.seckill.common.dto.OrderDTO;
import com.lyloou.seckill.common.repository.entity.TransactionLogEntity;
import com.lyloou.seckill.common.repository.service.TransactionLogService;
import com.lyloou.seckill.service.OrderApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lilou
 * @since 2021/6/15
 */
@Component
@Slf4j
public class OrderTransactionProducerListener implements TransactionListener {
    @Autowired
    OrderApiService orderApiService;

    @Autowired
    TransactionLogService transactionLogService;

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        log.debug("开始执行本地事务......");
        LocalTransactionState state;

        try {
            final String body = new String(msg.getBody());
            final OrderDTO order = JSONUtil.toBean(body, OrderDTO.class);
            orderApiService.order(order, msg.getTransactionId());

            state = LocalTransactionState.COMMIT_MESSAGE;
            log.debug("本地事务已经提交. {}", msg.getTransactionId());
        } catch (Exception e) {
            log.warn("执行本地事务失败", e);
            state = LocalTransactionState.ROLLBACK_MESSAGE;
        }

        return state;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        log.debug("开始回查本地事务......");
        LocalTransactionState state;
        final String transactionId = msg.getTransactionId();
        if (transactionLogService.lambdaQuery()
                .eq(TransactionLogEntity::getId, transactionId)
                .count() > 0) {
            state = LocalTransactionState.COMMIT_MESSAGE;
        } else {
            state = LocalTransactionState.ROLLBACK_MESSAGE;
        }
        log.debug("结束本地事务状态查询：{}", state);
        return state;
    }
}
