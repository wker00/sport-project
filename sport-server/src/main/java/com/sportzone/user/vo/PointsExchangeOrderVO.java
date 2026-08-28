package com.sportzone.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsExchangeOrderVO {

    private Long id;
    private String orderNo;
    private Long giftId;
    private String giftName;
    private String giftImage;
    private Integer pointsPrice;

    private Integer status;

    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String address;

    private String logisticsCompany;
    private String logisticsNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime shipTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime completeTime;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime createTime;
}