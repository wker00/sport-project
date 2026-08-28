package com.sportzone.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminCreateCouponDTO {

    @NotBlank(message = "优惠券名称不能为空")
    @Size(max = 50, message = "优惠券名称不能超过50个字符")
    private String name;

    @NotNull(message = "类型不能为空")
    @Min(value = 1, message = "类型值不正确")
    @Max(value = 2, message = "类型值不正确")
    private Integer type;

    @NotNull(message = "优惠值不能为空")
    @DecimalMin(value = "0.01", message = "优惠值必须大于0")
    private BigDecimal value;

    @DecimalMin(value = "0", message = "使用门槛不能为负数")
    private BigDecimal minAmount;

    @Min(value = 0, message = "积分不能为负数")
    private Integer pointsCost;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @NotNull(message = "生效时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    @NotNull(message = "过期时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;
}
