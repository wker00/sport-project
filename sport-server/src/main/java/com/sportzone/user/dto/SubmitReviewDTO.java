package com.sportzone.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "提交评价请求")
public class SubmitReviewDTO {

    @NotNull(message = "订单商品ID不能为空")
    @Schema(description = "订单商品ID")
    private Long orderItemId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1星")
    @Max(value = 5, message = "评分最高为5星")
    @Schema(description = "评分（1-5星）")
    private Integer rating;

    @Size(max = 500, message = "评价内容不能超过500个字符")
    @Schema(description = "评价内容")
    private String reviewContent;

    @Schema(description = "评价图片JSON数组，如[\"url1\",\"url2\"]")
    private String reviewImages;
}
