package com.sportzone.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewReplyDTO {

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不能超过500个字符")
    private String replyContent;
}
