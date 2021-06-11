package com.lyloou.seckill.common.dto;

import com.lyloou.component.dto.DTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value = "Order对象", description = "订单实体")
@NoArgsConstructor
public class OrderDTO extends DTO {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "实体ID")
    private Integer id;


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

    @ApiModelProperty(value = "创建时间")
    private Date createdTime;

    @ApiModelProperty(value = "更新时间")
    private Date updatedTime;

}
