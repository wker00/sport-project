package com.sportzone.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_points_record")
public class PointsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer type;
    private Long points;
    private String source;
    private String referenceNo;
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}