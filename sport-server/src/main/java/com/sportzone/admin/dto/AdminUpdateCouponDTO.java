package com.sportzone.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminUpdateCouponDTO {

    private String name;
    private Integer type;
    private BigDecimal value;
    private BigDecimal minAmount;
    private Integer pointsCost;
    private Integer stock;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;
}
