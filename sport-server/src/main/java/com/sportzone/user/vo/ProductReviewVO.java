package com.sportzone.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductReviewVO {

    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer userLevel;
    private Integer rating;
    private String reviewContent;
    private String reviewImages;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime reviewTime;

    private String replyContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime replyTime;
}
