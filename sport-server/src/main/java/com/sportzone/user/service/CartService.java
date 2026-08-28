package com.sportzone.user.service;

import com.sportzone.user.dto.CartDTO;
import com.sportzone.user.dto.UpdateCartDTO;
import com.sportzone.user.vo.CartVO;

import java.util.List;

public interface CartService {

    Long addCart(Long userId, CartDTO dto);

    void updateCart(Long userId, Long id, UpdateCartDTO dto);

    void deleteCart(Long userId, Long id);

    void clearCart(Long userId);

    List<CartVO> getCartList(Long userId);

    void checkCart(Long userId, Long id, Integer checked);
}