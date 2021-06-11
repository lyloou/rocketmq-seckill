package com.lyloou.seckill.common.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 模板表
 * </p>
 *
 * @author lilou
 * @since 2021-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_order")
@ApiModel(value = "OrderEntity对象", description = "模板表")
public class OrderEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单序列号")
    private String orderNo;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "产品ID")
    private String productId;

    @ApiModelProperty(value = "订单状态")
    private String orderStatus;

    @ApiModelProperty(value = "订单内容")
    private String content;

    @ApiModelProperty(value = "是否删除：0未删除，1已删除")
    private Integer deleted;


}
