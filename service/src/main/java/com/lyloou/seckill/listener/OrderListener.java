package com.lyloou.seckill.listener;

import cn.hutool.json.JSONUtil;
import com.lyloou.seckill.common.dto.OrderDTO;
import com.lyloou.seckill.common.dto.PayResultDTO;
import com.lyloou.seckill.common.dto.PayStatus;
import com.lyloou.seckill.service.OrderManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lilou
 * @since 2021/6/11
 */
@Component
@RocketMQMessageListener(
        topic = "tp_seckill_order",
        consumerGroup = "grp_seckill_order",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING,
        secretKey = "*"
)
@Slf4j
public class OrderListener implements RocketMQListener<OrderDTO> {

    @Autowired
    OrderManagerService managerService;
    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(OrderDTO orderDTO) {
        log.info("收到订单信息：{}", orderDTO);

        // 插入数据库
        final boolean result = managerService.insert(orderDTO);
        log.info("插入订单：{}，结果：{}", orderDTO, result);

        // 如果支付超时，则发送取消订单的消息
        final PayResultDTO resultDTO = new PayResultDTO();
        resultDTO.setOrderNo(orderDTO.getOrderNo());
        resultDTO.setPayNo(null);
        resultDTO.setPayStatus(PayStatus.CANCEL.name());
        final String str = JSONUtil.toJsonStr(resultDTO);
        log.info("发送消息：{}", str);
        Message message = new Message("tp_seckill_pay", str.getBytes());
        rocketMQTemplate.asyncSend(message.getTopic(),
                RocketMQUtil.convertToSpringMessage(message),
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("发送成功：{}", sendResult);
                    }

                    @Override
                    public void onException(Throwable e) {
                    }
                }, 10000, 4);
    }
}
