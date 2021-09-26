package com.lyloou.seckill.service;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.json.JSONUtil;
import com.lyloou.component.exceptionhandler.exception.BizException;
import com.lyloou.component.redismanager.RedisService;
import com.lyloou.seckill.common.config.IdGenerator;
import com.lyloou.seckill.common.convertor.OrderConvertor;
import com.lyloou.seckill.common.dto.Constant;
import com.lyloou.seckill.common.dto.OrderDTO;
import com.lyloou.seckill.common.dto.OrderStatus;
import com.lyloou.seckill.common.dto.PayResultDTO;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        // redis 分布式锁，不能加在这里（因为：基于AOP原理，【事务】在【锁】的外层，先执行了【事务】，再【加锁】。正确的顺序应该是先【加锁】，再【执行事务】）
        // redisService.doWithLock("decr-stock::" + order.getProductId(), 100000, result -> {
        // 检查库存
        checkStock(order.getProductId());

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

        // sleep();
        // });

    }

    private void sleep() {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 【下单】前端调用，只用于向 rocketMQ 发送事务half消息
     */
    public void order(OrderDTO order) {
        checkStock(order.getProductId());

        order.setId(idGenerator.nextId());
        order.setOrderNo(idGenerator.nextIdStr());
        try {
            log.debug("开始发送");
            final TransactionSendResult sendResult = orderTransactionProducer.send(JSONUtil.toJsonStr(order), Constant.TOPIC_ORDER);
            log.debug("发送完成");
            if (Objects.equals(sendResult.getLocalTransactionState(), LocalTransactionState.COMMIT_MESSAGE)) {
                log.debug("事务执行成功：{}", sendResult);
            } else {
                log.warn("事务执行异常：{}", sendResult);
                // 再次检查库存
                checkStock(order.getProductId());

                // 不是库存异常，抛出其他异常
                throw new BizException("下单失败，其他异常");
            }
        } catch (MQClientException e) {
            throw new BizException("下订单失败", e);
        }
    }

    private void checkStock(String productId) {
        // 检查库存
        Integer stock = stockApiService.getStock(productId);
        if (stock <= 0) {
            throw new BizException("商品" + productId + "无库存");
        }
    }

    private final int nThreads = Runtime.getRuntime().availableProcessors();
    private final ExecutorService poolExecutor = new ThreadPoolExecutor(
            nThreads,
            nThreads,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            ThreadFactoryBuilder.create().build(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );


    public void batchOrderPay() {
        for (int i = 0; i < 1000; i++) {
            poolExecutor.submit(newOrderRunnable(i));

        }
    }

    private Runnable newOrderRunnable(int i) {
        return () -> {
            try {

                OrderDTO order = new OrderDTO();
                order.setUserId("user-id:" + i);
                order.setProductId("1");
                order.setContent("content-" + i);
                order.setOrderStatus(OrderStatus.NEW.name());
                order(order);

                PayResultDTO payResultDTO = new PayResultDTO();
                payResultDTO.setOrderNo(order.getOrderNo());
                payResultDTO.setPayNo("pay-no" + i);
                final boolean result = payApiService.pay(payResultDTO);
                log.info("第{}个，支付结果：{}", i, result);
            } catch (Exception e) {
                log.error("第{}个，异常：{}", i, e);
            }
        };
    }

}
