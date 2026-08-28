package com.sportzone.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime updateTime;
}