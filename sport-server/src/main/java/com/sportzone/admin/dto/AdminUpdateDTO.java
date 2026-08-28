package com.sportzone.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminUpdateDTO {

    @Size(min = 3, max = 20, message = "昵称长度必须在3到20个字符之间")
    private String nickname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Min(value = 1, message = "角色值无效")
    @Max(value = 2, message = "角色值无效")
    private Integer role;
}
