package com.sportzone.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginDTO {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
