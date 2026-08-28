package com.sportzone.user.vo;

import lombok.Data;

@Data
public class SigninStatusVO {
    private boolean signedIn;
    private int streakDays;
    private int todayBonus;
}
