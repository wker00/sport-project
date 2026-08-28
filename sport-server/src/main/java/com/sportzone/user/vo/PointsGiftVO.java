package com.sportzone.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsGiftVO {

    private Long id;
    private String name;
    private String image;
    private Integer pointsPrice;
    private Integer stock;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime createTime;
}