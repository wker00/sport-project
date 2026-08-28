package com.sportzone.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_statistics_daily")
public class StatisticsDaily {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Integer newUsers;
    private Integer activeUsers;
    private Integer newOrders;
    private BigDecimal orderAmount;
    private BigDecimal payAmount;
    private BigDecimal refundAmount;
    private LocalDateTime createTime;
}
