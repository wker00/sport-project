package com.sportzone.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyNowDTO {

    @NotNull(message = "商品ID不能为空")
    @Min(value = 1, message = "商品ID无效")
    private Long productId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量不能小于1")
    private Integer quantity;

    private String spec;

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    private String remark;

    private Long couponId;
}
