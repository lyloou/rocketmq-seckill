package com.lyloou.seckill.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlusConfig
 *
 * @author lilou
 **/
@Configuration
@MapperScan("com.lyloou.seckill.*.repository.mapper")
public class MybatisPlusConfig {
    @Bean
    public MetaObjectHandler mybatisObjectHandler() {
        return new MybatisObjectHandler();
    }


}
