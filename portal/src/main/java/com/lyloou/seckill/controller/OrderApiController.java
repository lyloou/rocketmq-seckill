package com.lyloou.seckill.controller;

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


    @PostMapping("/batch-order-pay")
    public SingleResponse<String> batchOrderPay() {
        orderApiService.batchOrderPay();
        return SingleResponse.buildSuccess("success");
    }
}
