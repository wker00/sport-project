package com.sportzone.user.vo;

import lombok.Data;

@Data
public class SigninVO {
    private int pointsEarned;
    private int streakDays;
    private int bonusPoints;
    private long totalBalance;
}
