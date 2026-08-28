package com.sportzone.user.vo;

import lombok.Data;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private String image;
    private String icon;
    private String description;
    private Integer sortOrder;
}
