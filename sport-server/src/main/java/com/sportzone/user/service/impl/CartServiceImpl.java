package com.sportzone.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportzone.user.dto.CartDTO;
import com.sportzone.user.dto.UpdateCartDTO;
import com.sportzone.user.entity.Cart;
import com.sportzone.user.entity.Product;
import com.sportzone.user.mapper.CartMapper;
import com.sportzone.user.mapper.ProductMapper;
import com.sportzone.user.service.CartService;
import com.sportzone.user.vo.CartVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public CartServiceImpl(CartMapper cartMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
    }

    @Override
    public Long addCart(Long userId, CartDTO dto) {
        Product product = productMapper.selectOne(
                new LambdaQueryWrapper<Product>().eq(Product::getId, dto.getProductId()));
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 同一用户、同一商品、同一规格才合并数量
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getProductId, dto.getProductId());
        if (dto.getSpec() != null) {
            wrapper.eq(Cart::getSpec, dto.getSpec());
        } else {
            wrapper.isNull(Cart::getSpec);
        }
        Cart existing = cartMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            cartMapper.updateById(existing);
            return existing.getId();
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(dto.getProductId());
            cart.setQuantity(dto.getQuantity());
            cart.setSpec(dto.getSpec());
            cart.setChecked(1);
            cartMapper.insert(cart);
            return cart.getId();
        }
    }

    @Override
    public void updateCart(Long userId, Long id, UpdateCartDTO dto) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new RuntimeException("购物车记录不存在");
        }
        if (dto.getQuantity() != null) cart.setQuantity(dto.getQuantity());
        if (dto.getSpec() != null) cart.setSpec(dto.getSpec());
        if (dto.getChecked() != null) cart.setChecked(dto.getChecked());
        cartMapper.updateById(cart);
    }

    @Override
    public void deleteCart(Long userId, Long id) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new RuntimeException("购物车记录不存在");
        }
        cartMapper.deleteById(id);
    }

    @Override
    public void clearCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);
    }

    @Override
    public List<CartVO> getCartList(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.orderByDesc(Cart::getCreateTime);
        List<Cart> cartList = cartMapper.selectList(wrapper);

        return cartList.stream().map(cart -> {
            CartVO vo = new CartVO();
            vo.setId(cart.getId());
            vo.setProductId(cart.getProductId());
            vo.setQuantity(cart.getQuantity());
            vo.setSpec(cart.getSpec());
            vo.setChecked(cart.getChecked());
            vo.setCreateTime(cart.getCreateTime());

            Product product = productMapper.selectById(cart.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductImage(product.getImage());
                // 根据规格查找对应的价格和库存
                vo.setPrice(ProductServiceImpl.getSpecPrice(product.getSpecs(), cart.getSpec(), product.getPrice()));
                vo.setStock(ProductServiceImpl.getSpecStock(product.getSpecs(), cart.getSpec(), product.getStock()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void checkCart(Long userId, Long id, Integer checked) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new RuntimeException("购物车记录不存在");
        }
        cart.setChecked(checked);
        cartMapper.updateById(cart);
    }
}