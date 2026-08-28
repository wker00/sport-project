package com.sportzone.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartDTO {

    @Min(value = 1, message = "数量不能小于1")
    private Integer quantity;

    private String spec;

    private Integer checked;
}