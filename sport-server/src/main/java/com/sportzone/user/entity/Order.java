package com.sportzone.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal pointsDiscount;
    private BigDecimal levelDiscount;
    private BigDecimal payAmount;
    private Integer status;
    private Long addressId;
    private String remark;
    private String expressCompany;
    private String expressNo;
    private LocalDateTime deliveryTime;
    private LocalDateTime signTime;
    private Integer refundStatus;
    private String refundReason;
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}