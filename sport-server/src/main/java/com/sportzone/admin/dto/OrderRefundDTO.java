package com.sportzone.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRefundDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "请选择处理方式")
    private Integer refundStatus;

    private String refundReason;
}
