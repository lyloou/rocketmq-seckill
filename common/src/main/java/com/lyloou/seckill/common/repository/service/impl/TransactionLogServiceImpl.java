package com.lyloou.seckill.common.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyloou.seckill.common.repository.entity.TransactionLogEntity;
import com.lyloou.seckill.common.repository.mapper.TransactionLogMapper;
import com.lyloou.seckill.common.repository.service.TransactionLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lilou
 * @since 2021-06-15
 */
@Service
public class TransactionLogServiceImpl extends ServiceImpl<TransactionLogMapper, TransactionLogEntity> implements TransactionLogService {

}
