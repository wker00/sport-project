package com.sportzone.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponRecordVO {

    private Long id;
    private Long userId;
    private String username;
    private Long couponId;
    private Integer status;
    private Long orderId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime useTime;
}
