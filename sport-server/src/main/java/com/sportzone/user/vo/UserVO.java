package com.sportzone.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private String avatar;
    private String nickname;
    private Integer userLevel;
    private Long pointsBalance;
    private BigDecimal totalConsumption;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime createTime;
}