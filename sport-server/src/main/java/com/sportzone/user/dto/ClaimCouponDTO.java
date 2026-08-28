package com.sportzone.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimCouponDTO {

    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;
}
