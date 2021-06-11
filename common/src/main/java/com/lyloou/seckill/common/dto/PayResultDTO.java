package com.lyloou.seckill.common.dto;

import com.lyloou.component.dto.DTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value = "PayResultDTO对象", description = "支付结果实体")
public class PayResultDTO extends DTO {

    @ApiModelProperty(value = "订单序列号")
    private String orderNo;

    @ApiModelProperty(value = "支付序列号")
    private String payNo;

    @ApiModelProperty(value = "支付状态")
    private String payStatus;

}
