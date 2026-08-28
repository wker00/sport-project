package com.sportzone.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminReviewVO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private Long productId;
    private String productName;
    private String productImage;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer rating;
    private String reviewContent;
    private String reviewImages;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime reviewTime;

    private String replyContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private LocalDateTime replyTime;
}
