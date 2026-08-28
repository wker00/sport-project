package com.sportzone.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRegisterDTO {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String nickname;

    private String phone;

    private String email;

    private Integer role;
}
