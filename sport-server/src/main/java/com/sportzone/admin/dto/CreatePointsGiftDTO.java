package com.sportzone.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePointsGiftDTO {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String name;

    private String image;

    @NotNull(message = "所需积分不能为空")
    @Min(value = 0, message = "积分不能为负数")
    private Integer pointsPrice;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @Size(max = 255, message = "描述不能超过255个字符")
    private String description;
}
