package com.sportzone.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
    private Integer role;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime createTime;
}
