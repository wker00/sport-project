package com.sportzone.common.enums;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum UserLevelEnum {

    NORMAL(1, "普通会员", 1.0),
    SILVER(2, "银卡会员", 0.95),
    GOLD(3, "金卡会员", 0.9),
    DIAMOND(4, "钻石会员", 0.85),
    BLACK(5, "黑金会员", 0.8);

    private final int level;
    private final String name;
    private final double discountRate;

    UserLevelEnum(int level, String name, double discountRate) {
        this.level = level;
        this.name = name;
        this.discountRate = discountRate;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public static UserLevelEnum fromLevel(int level) {
        for (UserLevelEnum e : values()) {
            if (e.level == level) {
                return e;
            }
        }
        return NORMAL;
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = BigDecimal.valueOf(discountRate);
        BigDecimal discounted = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return amount.subtract(discounted);
    }
}
