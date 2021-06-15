package com.lyloou.seckill.service;

import cn.hutool.json.JSONUtil;
import com.lyloou.component.exceptionhandler.exception.BizException;
import com.lyloou.component.redismanager.RedisService;
import com.lyloou.seckill.common.config.IdGenerator;
import com.lyloou.seckill.common.convertor.OrderConvertor;
import com.lyloou.seckill.common.dto.Constant;
import com.lyloou.seckill.common.dto.OrderDTO;
import com.lyloou.seckill.common.repository.entity.TransactionLogEntity;
import com.lyloou.seckill.common.repository.service.OrderService;
import com.lyloou.seckill.common.repository.service.TransactionLogService;
import com.lyloou.seckill.mq.OrderTransactionProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author lilou
 * @since 2021/6/11
 */
@Service
@Slf4j
public class OrderApiService {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderConvertor convertor;

    @Autowired
    RedisService redisService;

    @Autowired
    IdGenerator idGenerator;

    @Autowired
    StockApiService stockApiService;

    @Autowired
    OrderTransactionProducer orderTransactionProducer;

    @Autowired
    TransactionLogService transactionLogService;

    @Autowired
    PayApiService payApiService;

    /**
     * 【下单】事务调用
     * 执行本地事务时调用，将订单数据和事务日志写入数据库
     *
     * @param order
     * @param transactionId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void order(OrderDTO order, String transactionId) {
        // redis 分布式锁
        redisService.doWithLock("decr-stock::" + order.getProductId(), 3000, result -> {
            // 检查库存
            Integer stock = stockApiService.getStock(order.getProductId());
            if (stock <= 0) {
                throw new BizException("无库存");
            }

            // 扣减库存
            stockApiService.decrStock(order.getProductId());

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

            payApiService.cancelPayIfTimeout(order.getOrderNo());

            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * 【下单】前端调用，只用于向 rocketMQ 发送事务half消息
     */
    public void order(OrderDTO order) {
        order.setId(idGenerator.nextId());
        order.setOrderNo(idGenerator.nextIdStr());
        try {
            log.debug("开始发送");
            final TransactionSendResult sendResult = orderTransactionProducer.send(JSONUtil.toJsonStr(order), Constant.TOPIC_ORDER);
            log.debug("发送完成");
            if (Objects.equals(sendResult.getLocalTransactionState(), LocalTransactionState.COMMIT_MESSAGE)) {
                log.info("事务执行成功：{}", sendResult);
            } else {
                log.warn("事务执行异常：{}", sendResult);
            }
        } catch (MQClientException e) {
            throw new BizException("下订单失败", e);
        }
    }

}
