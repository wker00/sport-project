package com.sportzone.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDTO {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    private String remark;

    @NotEmpty(message = "请选择要购买的商品")
    private List<Long> cartItemIds;

    private Long couponId;
}