package com.sportzone.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "退款申请请求")
public class RefundDTO {

    @Size(max = 255, message = "退款原因不能超过255个字符")
    @Schema(description = "退款原因（可选）")
    private String refundReason;
}
