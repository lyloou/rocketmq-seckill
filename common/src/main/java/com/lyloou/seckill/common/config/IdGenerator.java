package com.lyloou.seckill.common.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

/**
 * @author lilou
 * @since 2021/6/11
 */
@Component
public class IdGenerator {
    private static final Snowflake snowflake = IdUtil.createSnowflake(1, 1);

    public String nextIdStr() {
        return snowflake.nextIdStr();
    }
}
