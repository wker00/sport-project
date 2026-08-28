package com.sportzone.admin.dto;

import lombok.Data;

@Data
public class UpdateCategoryDTO {

    private String name;
    private String image;
    private String icon;
    private String description;
    private Integer sortOrder;
}
