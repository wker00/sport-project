package com.sportzone.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_cart")
public class Cart {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private String spec;
    private Integer checked;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}