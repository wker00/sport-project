package com.sportzone.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sportzone.user.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM sys_order WHERE user_id = #{userId} AND status IN (4, 5) AND is_deleted = 0")
    BigDecimal selectTotalSpent(Long userId);
}