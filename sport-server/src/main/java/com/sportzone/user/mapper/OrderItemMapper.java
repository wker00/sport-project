package com.sportzone.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sportzone.user.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}