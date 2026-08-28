package com.sportzone.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OperateLogEvent {

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
}
