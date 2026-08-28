package com.sportzone.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sportzone.user.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}