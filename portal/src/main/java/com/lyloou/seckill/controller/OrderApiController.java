package com.lyloou.seckill.controller;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lyloou.component.dto.SingleResponse;
import com.lyloou.seckill.common.dto.OrderDTO;
import com.lyloou.seckill.common.dto.OrderStatus;
import com.lyloou.seckill.common.dto.PayResultDTO;
import com.lyloou.seckill.service.OrderApiService;
import com.lyloou.seckill.service.PayApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lilou
 * @since 2021/6/11
 */
@RestController
@Slf4j
public class OrderApiController {

    @Autowired
    OrderApiService orderApiService;
    @Autowired
    PayApiService payApiService;

    @PostMapping("/order")
    public SingleResponse<String> order(OrderDTO order) {
        order.setOrderStatus(OrderStatus.NEW.name());
        orderApiService.order(order);
        return SingleResponse.buildSuccess(order.getOrderNo());
    }

    @PostMapping("/pay")
    public SingleResponse<String> pay(PayResultDTO payResultDTO) {
        boolean result = payApiService.pay(payResultDTO);
        return SingleResponse.buildSuccess(result ? "success" : "fail");
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


    @PostMapping("/batch-order-pay")
    public SingleResponse<String> batchOrder() {
        for (int i = 0; i < 100; i++) {
            poolExecutor.submit(newOrderRunnable(i));

        }
        return SingleResponse.buildSuccess("success");
    }

    private Runnable newOrderRunnable(int i) {
        return () -> {
            try {

                OrderDTO order = new OrderDTO();
                order.setUserId("user-id:" + i);
                order.setProductId("1");
                order.setContent("content-" + i);
                order.setOrderStatus(OrderStatus.NEW.name());
                orderApiService.order(order);

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
