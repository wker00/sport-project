package com.sportzone.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_points_exchange_order")
public class PointsExchangeOrder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long giftId;
    private String giftName;
    private String giftImage;
    private Integer pointsPrice;

    private Integer status;

    private Long addressId;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String address;

    private String logisticsCompany;
    private String logisticsNo;

    private LocalDateTime shipTime;
    private LocalDateTime completeTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}