package com.sportzone.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_coupon")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer type;
    private BigDecimal value;
    private BigDecimal minAmount;
    private Integer pointsCost;
    private Integer stock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
