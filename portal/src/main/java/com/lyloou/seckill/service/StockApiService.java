package com.lyloou.seckill.service;

import com.lyloou.component.redismanager.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * @author lilou
 * @since 2021/6/11
 */
@Service
public class StockApiService {
    @Autowired
    RedisService redisService;

    @PostConstruct
    public void init() {
        redisService.set("product::" + 1, String.valueOf(10));
    }

    public Integer getStock(String productId) {
        productId = "1";
        final String stock = redisService.get("product::" + productId);
        return Optional.ofNullable(stock).map(Integer::valueOf).orElse(0);
    }

    public void decrStock(String productId) {
        productId = "1";
        redisService.decr("product::" + productId);
    }
}
