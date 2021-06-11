package com.lyloou.seckill.listener;

import cn.hutool.json.JSONUtil;
import com.google.common.base.Strings;
import com.lyloou.seckill.common.dto.PayResultDTO;
import com.lyloou.seckill.service.OrderManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lilou
 * @since 2021/6/11
 */
@Component
@RocketMQMessageListener(
        topic = "tp_seckill_pay",
        consumerGroup = "grp_seckill_pay",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING,
        secretKey = "*"
)
@Slf4j
public class PayListener implements RocketMQListener {
    @Autowired
    OrderManagerService orderManagerService;

    @Override
    public void onMessage(Object o) {
        log.info("收到支付信息：{}", o);

        PayResultDTO payResultDTO = JSONUtil.toBean(JSONUtil.toJsonStr(o), PayResultDTO.class);
        final String orderNo = payResultDTO.getOrderNo();
        if (Strings.isNullOrEmpty(orderNo)) {
            log.warn("无效的消息" + o);
            return;
        }
        orderManagerService.handle(payResultDTO);
    }
}
