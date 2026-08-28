package com.sportzone.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderShipDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "快递公司不能为空")
    private String expressCompany;

    @NotBlank(message = "快递单号不能为空")
    private String expressNo;
}
