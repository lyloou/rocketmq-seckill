package com.lyloou.seckill.service;

import cn.hutool.json.JSONUtil;
import com.lyloou.component.redismanager.RedisService;
import com.lyloou.seckill.common.config.IdGenerator;
import com.lyloou.seckill.common.dto.OrderDTO;
import com.lyloou.seckill.common.dto.PayResultDTO;
import com.lyloou.seckill.common.dto.PayStatus;
import com.lyloou.seckill.common.repository.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    RedisService redisService;

    @Autowired
    IdGenerator idGenerator;

    @Autowired
    StockApiService stockApiService;

    public boolean order(OrderDTO order) {
        final String orderNo = idGenerator.nextIdStr();
        order.setOrderNo(orderNo);
        Integer stock = stockApiService.getStock(order.getProductId());
        if (stock <= 0) {
            return false;
        }

        rocketMQTemplate.asyncSend("tp_seckill_order", order, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                stockApiService.reduceStock(order.getProductId());
                log.info("下单成功，成功扣减库存，订单信息：{}", order);
            }

            @Override
            public void onException(Throwable e) {
                log.warn("下单失败，订单信息：{}，异常信息：{}", order, e);
            }
        });
        return true;
    }

    public boolean pay(PayResultDTO payResultDTO) {
        // 如果支付超时，则发送取消订单的消息
        final PayResultDTO resultDTO = new PayResultDTO();
        resultDTO.setOrderNo(payResultDTO.getOrderNo());
        resultDTO.setPayNo(payResultDTO.getPayNo());
        resultDTO.setPayStatus(PayStatus.PAYED.name());
        final String str = JSONUtil.toJsonStr(resultDTO);
        log.info("发送消息：{}", str);

        final org.springframework.messaging.Message message = RocketMQUtil.convertToSpringMessage(new Message("tp_seckill_pay", str.getBytes()));
        rocketMQTemplate.asyncSend("tp_seckill_pay", message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送支付信息，成功：{}", sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.warn("发送支付信息，失败", e);
            }
        }, 3000, 3);
        return true;
    }
}
