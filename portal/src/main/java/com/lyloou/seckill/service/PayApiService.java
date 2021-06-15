package com.lyloou.seckill.service;

import cn.hutool.json.JSONUtil;
import com.lyloou.component.exceptionhandler.exception.BizException;
import com.lyloou.seckill.common.dto.Constant;
import com.lyloou.seckill.common.dto.OrderStatus;
import com.lyloou.seckill.common.dto.PayResultDTO;
import com.lyloou.seckill.common.dto.PayStatus;
import com.lyloou.seckill.common.repository.entity.OrderEntity;
import com.lyloou.seckill.common.repository.entity.TransactionLogEntity;
import com.lyloou.seckill.common.repository.service.OrderService;
import com.lyloou.seckill.common.repository.service.TransactionLogService;
import com.lyloou.seckill.mq.PayTransactionProducer;
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
public class PayApiService {

    @Autowired
    PayTransactionProducer payTransactionProducer;

    @Autowired
    TransactionLogService transactionLogService;


    public boolean pay(PayResultDTO payResultDTO) {
        final PayResultDTO resultDTO = new PayResultDTO();
        resultDTO.setOrderNo(payResultDTO.getOrderNo());
        resultDTO.setPayNo(payResultDTO.getPayNo());
        resultDTO.setPayStatus(PayStatus.PAYED.name());
        final String str = JSONUtil.toJsonStr(resultDTO);
        checkOrderStatus(payResultDTO.getOrderNo());

        try {
            log.info("pay 开始发送：{}", str);
            final TransactionSendResult sendResult = payTransactionProducer.send(str, Constant.TOPIC_PAY);
            log.debug("pay 发送完成");
            if (Objects.equals(sendResult.getLocalTransactionState(), LocalTransactionState.COMMIT_MESSAGE)) {
                log.info("pay 事务执行成功：{}", sendResult);
                return true;
            } else {
                log.warn("pay 事务执行异常：{}", sendResult);
                return false;
            }
        } catch (MQClientException e) {
            throw new BizException("下订单失败", e);
        }
    }

    @Autowired
    OrderService orderService;

    private void checkOrderStatus(String orderNo) {
        final OrderEntity orderEntity = orderService.lambdaQuery()
                .eq(OrderEntity::getOrderNo, orderNo)
                .oneOpt()
                .orElseThrow(() -> new BizException("not valid order no"));
        if (!OrderStatus.NEW.name().equals(orderEntity.getOrderStatus())) {
            throw new BizException("order status is not valid: " + orderEntity.getOrderStatus());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void pay(PayResultDTO payResultDTO, String transactionId) {

        // 写入事务日志
        transactionLogService.save(new TransactionLogEntity()
                .setId(transactionId)
                .setBusiness("pay")
                .setForeignKey(String.valueOf(payResultDTO.getPayNo()))
        );
    }

    // cancelPayIfTimeout
    public void cancelPayIfTimeout(String orderNo) {
        final PayResultDTO resultDTO = new PayResultDTO();
        resultDTO.setOrderNo(orderNo);
        resultDTO.setPayNo(null);
        resultDTO.setPayStatus(PayStatus.CANCEL.name());

        try {
            final String str = JSONUtil.toJsonStr(resultDTO);
            log.info("cancelPayIfTimeout 异步发送：{}", str);
            payTransactionProducer.asyncSendWithDelayTimeLevel(str, Constant.TOPIC_PAY, 4);
        } catch (Exception e) {
            throw new BizException("cancelPayIfTimeout 失败", e);
        }
    }
}
