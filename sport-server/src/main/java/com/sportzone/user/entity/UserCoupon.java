package com.sportzone.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user_coupon")
public class UserCoupon {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId;
    private Integer status;
    private Long orderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime useTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
