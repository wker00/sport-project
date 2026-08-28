package com.sportzone.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperateLogVO {

    private Long id;
    private Long adminId;
    private String username;
    private String module;
    private String type;
    private String description;
    private String method;
    private String url;
    private String params;
    private String result;
    private String errorMsg;
    private String ip;
    private Long costTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime createTime;
}
