package com.lyloou.seckill.common.convertor;

import com.google.common.base.Converter;
import com.lyloou.seckill.common.dto.OrderDTO;
import com.lyloou.seckill.common.repository.entity.OrderEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @author lilou
 * @since 2021/6/11
 */
@Component
public class OrderConvertor extends Converter<OrderDTO, OrderEntity> {

    @Override
    protected OrderEntity doForward(OrderDTO orderDTO) {
        final OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(orderDTO, orderEntity);
        return orderEntity;
    }

    @Override
    protected OrderDTO doBackward(OrderEntity orderEntity) {
        final OrderDTO orderDTO = new OrderDTO();
        BeanUtils.copyProperties(orderEntity, orderDTO);
        return orderDTO;
    }
}
