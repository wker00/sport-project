package com.sportzone.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardVO {

    private Long totalUsers; // 用户总数
    private Long totalProducts; // 商品总数
    private Long totalOrders; // 订单总数
    private Long pendingShipOrders; // 待发货订单数
    private Long refundRequests; // 退货申请数
    private BigDecimal todayOrderAmount; // 今日订单金额
}
