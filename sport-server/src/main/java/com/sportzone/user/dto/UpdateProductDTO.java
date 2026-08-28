package com.sportzone.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductDTO {

    private Long categoryId;

    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String name;

    @Size(max = 255, message = "副标题不能超过255个字符")
    private String subtitle;

    @DecimalMin(value = "0.01", message = "售价必须大于0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;

    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @Min(value = 0, message = "销量不能为负数")
    private Integer salesCount;

    private String image;

    @Size(max = 20, message = "标签不能超过20个字符")
    private String badge;

    private String images;

    private String specs;

    private String detail;

    private Boolean isOn;
}