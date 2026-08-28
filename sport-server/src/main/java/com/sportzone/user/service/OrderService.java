package com.sportzone.user.service;

import com.sportzone.user.dto.BuyNowDTO;
import com.sportzone.user.dto.CreateOrderDTO;
import com.sportzone.user.dto.RefundDTO;
import com.sportzone.user.dto.SubmitReviewDTO;
import com.sportzone.user.vo.OrderVO;

import java.util.List;

public interface OrderService {

    OrderVO createOrder(Long userId, CreateOrderDTO dto);

    OrderVO buyNow(Long userId, BuyNowDTO dto);

    OrderVO getOrderDetail(Long userId, Long orderId);

    List<OrderVO> getOrderList(Long userId);

    void payOrder(Long userId, Long orderId);

    void cancelOrder(Long userId, Long orderId);

    void confirmReceipt(Long userId, Long orderId);

    void submitReview(Long userId, Long orderId, SubmitReviewDTO dto);

    void applyRefund(Long userId, Long orderId, RefundDTO dto);
}