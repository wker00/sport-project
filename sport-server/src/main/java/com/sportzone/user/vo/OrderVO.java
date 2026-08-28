package com.sportzone.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal levelDiscount;
    private BigDecimal payAmount;
    private Integer status;
    private String remark;
    private String expressCompany;
    private String expressNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime deliveryTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime signTime;
    private Integer refundStatus;
    private String refundReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime createTime;

    private List<OrderItemVO> items;
    private UserVO user;
    private AddressVO address;
}