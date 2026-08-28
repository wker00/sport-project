package com.sportzone.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sportzone.user.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}