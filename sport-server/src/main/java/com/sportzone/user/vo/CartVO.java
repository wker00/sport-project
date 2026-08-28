package com.sportzone.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartVO {

    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private String spec;
    private Integer checked;
    private Integer stock;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime createTime;
}