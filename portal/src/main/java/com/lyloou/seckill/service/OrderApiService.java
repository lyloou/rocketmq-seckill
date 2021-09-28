package com.lyloou.seckill.service;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.json.JSONUtil;
import com.lyloou.component.exceptionhandler.exception.BizException;
import com.lyloou.seckill.common.config.IdGenerator;
import com.lyloou.seckill.common.convertor.OrderConvertor;
import com.lyloou.seckill.common.dto.Constant;
import com.lyloou.seckill.common.dto.OrderDTO;
import com.lyloou.seckill.common.dto.OrderStatus;
import com.lyloou.seckill.common.dto.PayResultDTO;
import com.lyloou.seckill.common.repository.service.OrderService;
import com.lyloou.seckill.common.service.StockApiService;
import com.lyloou.seckill.mq.OrderTransactionProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    IdGenerator idGenerator;

    @Autowired
    StockApiService stockApiService;

    @Autowired
    OrderTransactionProducer orderTransactionProducer;


    @Autowired
    PayApiService payApiService;

    /**
     * 【下单】事务调用
     * 只完成库存的扣减，扣减成功就算下单成功
     */
    // @Transactional(rollbackFor = Exception.class)
    public void doOrder(OrderDTO order) {

        // 检查库存
        checkStock(order.getProductId());

        // 扣减库存
        stockApiService.decrStock(order.getProductId());

        // 超时取消订单，恢复库存
        payApiService.cancelPayIfTimeout(order.getOrderNo());
    }

    /**
     * 【下单】前端调用，只用于向 rocketMQ 发送事务half消息
     */
    public void order(OrderDTO order) {
        checkOrder(order);
        checkStock(order.getProductId());

        order.setId(idGenerator.nextId());
        order.setOrderNo(idGenerator.nextIdStr());

        try {
            final String data = JSONUtil.toJsonStr(order);
            final TransactionSendResult sendResult = orderTransactionProducer.send(data, Constant.TOPIC_ORDER);
            if (Objects.equals(sendResult.getLocalTransactionState(), LocalTransactionState.COMMIT_MESSAGE)) {
                log.debug("事务执行成功：{}", sendResult);
            } else {
                log.warn("事务执行异常：{}", sendResult);
                // 再次检查库存
                checkStock(order.getProductId());

                // 不是库存异常，抛出其他异常
                throw new BizException("竞争太激烈了，请重新试试~");
            }
        } catch (MQClientException e) {
            throw new BizException("下订单失败", e);
        }
    }

    private void checkOrder(OrderDTO order) {

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

                // 延迟支付
                poolExecutor.submit(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PayResultDTO payResultDTO = new PayResultDTO();
                    payResultDTO.setOrderNo(order.getOrderNo());
                    payResultDTO.setPayNo("pay-no" + order.getOrderNo());
                    final boolean result = payApiService.pay(payResultDTO);
                    log.info("第{}个，支付结果：{}", i, result);
                });
            } catch (Exception e) {
                log.error("第{}个，异常：{}", i, e);
            }
        };
    }

}
