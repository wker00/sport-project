package com.sportzone.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductDTO {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String name;

    @Size(max = 255, message = "副标题不能超过255个字符")
    private String subtitle;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    private String image;

    @Size(max = 20, message = "标签不能超过20个字符")
    private String badge;

    private String images;

    private String specs;

    private String detail;

    private Boolean isOn;
}