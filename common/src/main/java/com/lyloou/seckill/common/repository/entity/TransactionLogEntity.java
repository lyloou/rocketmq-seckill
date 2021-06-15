package com.lyloou.seckill.common.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lyloou.component.dto.DTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author lilou
 * @since 2021-06-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_transaction_log")
@ApiModel(value = "TransactionLogEntity对象", description = "")
public class TransactionLogEntity extends DTO {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "实体ID")
    private String id;

    @ApiModelProperty(value = "业务标识")
    private String business;

    @ApiModelProperty(value = "对应业务表中的主键")
    private String foreignKey;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;

}
