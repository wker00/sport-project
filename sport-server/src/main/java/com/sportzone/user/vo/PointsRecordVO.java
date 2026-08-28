package com.sportzone.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsRecordVO {

    private Long id;
    private Integer type;
    private Long points;
    private String source;
    private String referenceNo;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime createTime;
}