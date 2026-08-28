package com.sportzone.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String name;

    @Size(max = 500, message = "图片URL不能超过500个字符")
    private String image;

    @Size(max = 50, message = "图标不能超过50个字符")
    private String icon;

    @Size(max = 255, message = "描述不能超过255个字符")
    private String description;

    private Integer sortOrder;
}
