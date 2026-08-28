package com.sportzone.admin.dto;

import lombok.Data;

@Data
public class UpdatePointsGiftDTO {

    private String name;
    private String image;
    private Integer pointsPrice;
    private Integer stock;
    private String description;
}
