package com.sportzone.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExchangeDTO {

    @NotNull(message = "积分商品ID不能为空")
    private Long giftId;

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    private String remark;
}