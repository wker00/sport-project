package com.sportzone.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user_address")
public class UserAddress {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String address;
    private Integer isDefault;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}