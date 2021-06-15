package com.lyloou.seckill.service;

import com.google.common.base.Strings;
import com.lyloou.component.redismanager.RedisService;
import com.lyloou.seckill.common.dto.OrderStatus;
import com.lyloou.seckill.common.dto.PayResultDTO;
import com.lyloou.seckill.common.repository.entity.OrderEntity;
import com.lyloou.seckill.common.repository.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author lilou
 * @since 2021/6/11
 */
@Service
@Slf4j
public class OrderManagerService {

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;


    public void handle(PayResultDTO payResultDTO) {
        final String payNo = payResultDTO.getPayNo();
        final String orderNo = payResultDTO.getOrderNo();

        // 查订单
        final Optional<OrderEntity> entity = orderService.lambdaQuery()
                .eq(OrderEntity::getOrderNo, orderNo)
                .oneOpt();
        if (!entity.isPresent()) {
            log.warn("无效的订单号：{}，订单信息：{}", orderNo, payResultDTO);
            return;
        }

        // 订单已经支付，不用操作
        final OrderEntity orderEntity = entity.get();
        if (Objects.equals(orderEntity.getOrderStatus(), OrderStatus.PAYED.name())) {
            log.info("支付已经支付过了，无需操作：{}", orderEntity);
            return;
        }

        // 订单已经取消，不用操作
        if (Objects.equals(orderEntity.getOrderStatus(), OrderStatus.CANCEL.name())) {
            log.warn("支付已经取消：{}", orderEntity);
            return;
        }

        // 如果订单是新订单，通过 payNo 判断是否是合法的支付码
        if (Objects.equals(orderEntity.getOrderStatus(), OrderStatus.NEW.name())) {
            if (isValidPayNo(payNo)) {
                orderEntity.setOrderStatus(OrderStatus.PAYED.name());
                orderService.lambdaUpdate()
                        .eq(OrderEntity::getOrderNo, orderNo)
                        .update(orderEntity);
                return;
            }
        }

        // 其他情况，取消订单，恢复库存
        orderEntity.setOrderStatus(OrderStatus.CANCEL.name());
        orderService.lambdaUpdate()
                .eq(OrderEntity::getOrderNo, orderNo)
                .update(orderEntity);
        redisService.incr("product::" + 1);
    }

    // 不为空就认为是合法的
    private boolean isValidPayNo(String payNo) {
        return !Strings.isNullOrEmpty(payNo);
    }
}
