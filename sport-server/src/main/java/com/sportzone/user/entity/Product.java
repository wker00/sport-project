package com.sportzone.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String name;
    private String subtitle;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer salesCount;
    private BigDecimal ratingScore;
    private String badge;
    private String image;
    private String images;
    private String specs;
    private String detail;
    private Integer isOn;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}