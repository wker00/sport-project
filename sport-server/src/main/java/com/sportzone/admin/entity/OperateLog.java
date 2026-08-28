package com.sportzone.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operate_log")
public class OperateLog {

    @TableId(type = IdType.AUTO)
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

    @TableField(insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
}
