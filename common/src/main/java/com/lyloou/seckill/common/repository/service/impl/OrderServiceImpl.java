package com.lyloou.seckill.common.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyloou.seckill.common.repository.entity.OrderEntity;
import com.lyloou.seckill.common.repository.mapper.OrderMapper;
import com.lyloou.seckill.common.repository.service.OrderService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 模板表 服务实现类
 * </p>
 *
 * @author lilou
 * @since 2021-06-11
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

}
