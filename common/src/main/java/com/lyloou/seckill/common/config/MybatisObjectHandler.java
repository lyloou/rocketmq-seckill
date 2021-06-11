package com.lyloou.seckill.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;
import java.util.stream.Stream;

/**
 * https://baomidou.com/guide/auto-fill-metainfo.html
 * MybatisObjectHandler
 *
 * @author lilou
 **/
public class MybatisObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        Integer userId = null;
        if (userId != null) {
            Stream.of("createdBy", "updatedBy", "modifier", "creator")
                    .forEach(s -> setFieldValIfNull(s, userId.toString(), metaObject));
        }

        Stream.of("createdTime", "updatedTime")
                .forEach(s -> setFieldValIfNull(s, new Date(), metaObject));
    }

    private void setFieldValIfNull(String field, Object fieldVal, MetaObject metaObject) {
        final Object value = getFieldValByName(field, metaObject);
        if (value == null) {
            setFieldValByName(field, fieldVal, metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        //更新时 需要填充字段
        Stream.of("updatedTime")
                .forEach(s -> setFieldValIfNull(s, new Date(), metaObject));
        setFieldValByName("updatedTime", new Date(), metaObject);
        Integer userId = null;
        if (userId != null) {
            //更新时填充的字段
            Stream.of("updatedBy", "modifier")
                    .forEach(s -> setFieldValIfNull(s, userId.toString(), metaObject));
        }
    }
}
