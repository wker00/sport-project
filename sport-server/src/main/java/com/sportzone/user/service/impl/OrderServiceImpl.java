package com.sportzone.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportzone.common.enums.UserLevelEnum;
import com.sportzone.user.dto.BuyNowDTO;
import com.sportzone.user.dto.CreateOrderDTO;
import com.sportzone.user.dto.RefundDTO;
import com.sportzone.user.dto.SubmitReviewDTO;
import com.sportzone.user.entity.Cart;
import com.sportzone.user.entity.Coupon;
import com.sportzone.user.entity.Order;
import com.sportzone.user.entity.OrderItem;
import com.sportzone.user.entity.PointsRecord;
import com.sportzone.user.entity.Product;
import com.sportzone.user.entity.User;
import com.sportzone.user.entity.UserCoupon;
import com.sportzone.user.service.OrderService;
import com.sportzone.user.service.UserService;
import com.sportzone.user.mapper.CartMapper;
import com.sportzone.user.mapper.CouponMapper;
import com.sportzone.user.mapper.OrderItemMapper;
import com.sportzone.user.mapper.OrderMapper;
import com.sportzone.user.mapper.PointsRecordMapper;
import com.sportzone.user.mapper.ProductMapper;
import com.sportzone.user.mapper.UserCouponMapper;
import com.sportzone.user.mapper.UserMapper;
import com.sportzone.user.vo.OrderItemVO;
import com.sportzone.user.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final PointsRecordMapper pointsRecordMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;
    private final UserService userService;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            CartMapper cartMapper, ProductMapper productMapper,
                            UserMapper userMapper, PointsRecordMapper pointsRecordMapper,
                            UserCouponMapper userCouponMapper, CouponMapper couponMapper,
                            UserService userService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
        this.userMapper = userMapper;
        this.pointsRecordMapper = pointsRecordMapper;
        this.userCouponMapper = userCouponMapper;
        this.couponMapper = couponMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, CreateOrderDTO dto) {
        List<Cart> cartItems = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .in(Cart::getId, dto.getCartItemIds()));

        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车商品不存在");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Cart cart : cartItems) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) {
                throw new RuntimeException("商品「" + cart.getProductId() + "」不存在");
            }

            // 根据规格查找对应的价格和库存
            BigDecimal unitPrice = ProductServiceImpl.getSpecPrice(product.getSpecs(), cart.getSpec(), product.getPrice());
            int specStock = ProductServiceImpl.getSpecStock(product.getSpecs(), cart.getSpec(), product.getStock());

            if (specStock < cart.getQuantity()) {
                throw new RuntimeException("商品「" + product.getName() + "」库存不足");
            }

            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImage());
            item.setPrice(unitPrice);
            item.setQuantity(cart.getQuantity());
            item.setSpec(cart.getSpec());
            item.setPointsEarned(0L);
            orderItems.add(item);

            // 扣减商品总库存（有规格库存时同时扣减）
            product.setStock(product.getStock() - cart.getQuantity());
            productMapper.updateById(product);
        }

        User user = userMapper.selectById(userId);
        UserLevelEnum level = UserLevelEnum.fromLevel(user.getUserLevel());
        BigDecimal levelDiscount = level.calculateDiscount(totalAmount);
        BigDecimal afterLevelDiscount = totalAmount.subtract(levelDiscount);

        BigDecimal discountAmount = applyCoupon(userId, dto.getCouponId(), afterLevelDiscount);
        BigDecimal payAmount = afterLevelDiscount.subtract(discountAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) payAmount = BigDecimal.ZERO;

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setLevelDiscount(levelDiscount);
        order.setPointsDiscount(BigDecimal.ZERO);
        order.setPayAmount(payAmount);
        order.setStatus(0);
        order.setAddressId(dto.getAddressId());
        order.setRemark(dto.getRemark());
        order.setRefundStatus(0);
        orderMapper.insert(order);

        if (dto.getCouponId() != null) {
            markCouponUsed(dto.getCouponId(), order.getId());
        }

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .in(Cart::getId, dto.getCartItemIds()));

        return toVO(order);
    }

    @Override
    @Transactional
    public OrderVO buyNow(Long userId, BuyNowDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 根据规格查找对应的价格和库存
        BigDecimal unitPrice = ProductServiceImpl.getSpecPrice(product.getSpecs(), dto.getSpec(), product.getPrice());
        int specStock = ProductServiceImpl.getSpecStock(product.getSpecs(), dto.getSpec(), product.getStock());

        if (specStock < dto.getQuantity()) {
            throw new RuntimeException("商品「" + product.getName() + "」库存不足");
        }

        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(dto.getQuantity()));

        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getImage());
        item.setPrice(unitPrice);
        item.setQuantity(dto.getQuantity());
        item.setSpec(dto.getSpec());
        item.setPointsEarned(0L);

        product.setStock(product.getStock() - dto.getQuantity());
        productMapper.updateById(product);

        User user = userMapper.selectById(userId);
        UserLevelEnum level = UserLevelEnum.fromLevel(user.getUserLevel());
        BigDecimal levelDiscount = level.calculateDiscount(totalAmount);
        BigDecimal afterLevelDiscount = totalAmount.subtract(levelDiscount);

        BigDecimal discountAmount = applyCoupon(userId, dto.getCouponId(), afterLevelDiscount);
        BigDecimal payAmount = afterLevelDiscount.subtract(discountAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) payAmount = BigDecimal.ZERO;

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setLevelDiscount(levelDiscount);
        order.setPointsDiscount(BigDecimal.ZERO);
        order.setPayAmount(payAmount);
        order.setStatus(0);
        order.setAddressId(dto.getAddressId());
        order.setRemark(dto.getRemark());
        order.setRefundStatus(0);
        orderMapper.insert(order);

        if (dto.getCouponId() != null) {
            markCouponUsed(dto.getCouponId(), order.getId());
        }

        item.setOrderId(order.getId());
        orderItemMapper.insert(item);

        return toVO(order);
    }

    @Override
    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        return toVO(order);
    }

    @Override
    public List<OrderVO> getOrderList(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void payOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态不允许支付");
        }
        order.setStatus(1);
        orderMapper.updateById(order);

        // 支付后累加销量
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setSalesCount(product.getSalesCount() + item.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态不允许取消");
        }

        order.setStatus(6);
        orderMapper.updateById(order);

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    @Override
    @Transactional
    public void confirmReceipt(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 3) {
            throw new RuntimeException("当前订单状态不允许确认收货");
        }

        order.setStatus(4);
        order.setSignTime(LocalDateTime.now());
        orderMapper.updateById(order);

        long points = order.getPayAmount().longValue() * 10;
        if (points > 0) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setPointsBalance(user.getPointsBalance() + points);
                userMapper.updateById(user);

                PointsRecord record = new PointsRecord();
                record.setUserId(userId);
                record.setType(1);
                record.setPoints(points);
                record.setSource("order");
                record.setReferenceNo(order.getOrderNo());
                record.setDescription("订单完成获得" + points + "积分");
                pointsRecordMapper.insert(record);
            }
        }

        userService.upgradeLevel(userId);
    }

    @Override
    @Transactional
    public void submitReview(Long userId, Long orderId, SubmitReviewDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 4) {
            throw new RuntimeException("当前订单状态不允许评价");
        }

        OrderItem item = orderItemMapper.selectById(dto.getOrderItemId());
        if (item == null || !item.getOrderId().equals(orderId)) {
            throw new RuntimeException("订单商品不存在");
        }
        if (item.getRating() != null) {
            throw new RuntimeException("该商品已评价，不可重复评价");
        }

        item.setRating(dto.getRating());
        item.setReviewContent(dto.getReviewContent());
        String reviewImages = dto.getReviewImages();
        item.setReviewImages(reviewImages != null && reviewImages.isEmpty() ? null : reviewImages);
        item.setReviewTime(LocalDateTime.now());
        orderItemMapper.updateById(item);

        long reviewPoints = 50;
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPointsBalance(user.getPointsBalance() + reviewPoints);
            userMapper.updateById(user);

            PointsRecord record = new PointsRecord();
            record.setUserId(userId);
            record.setType(1);
            record.setPoints(reviewPoints);
            record.setSource("review");
            record.setReferenceNo(order.getOrderNo());
            record.setDescription("评价商品获得" + reviewPoints + "积分");
            pointsRecordMapper.insert(record);
        }

        long unratedCount = orderItemMapper.selectCount(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .isNull(OrderItem::getRating));
        if (unratedCount == 0) {
            order.setStatus(5);
            orderMapper.updateById(order);
        }

        updateProductRating(item.getProductId());
    }

    @Override
    @Transactional
    public void applyRefund(Long userId, Long orderId, RefundDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() < 1 || order.getStatus() > 4) {
            throw new RuntimeException("当前订单状态不允许退款");
        }
        if (order.getRefundStatus() != null && order.getRefundStatus() == 1) {
            throw new RuntimeException("已有退款申请处理中，不可重复提交");
        }
        order.setRefundStatus(1);
        if (dto.getRefundReason() != null && !dto.getRefundReason().isEmpty()) {
            order.setRefundReason(dto.getRefundReason());
        }
        orderMapper.updateById(order);
    }

    private void updateProductRating(Long productId) {
        List<OrderItem> ratedItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getProductId, productId)
                        .isNotNull(OrderItem::getRating));

        if (ratedItems.isEmpty()) return;

        double avg = ratedItems.stream()
                .mapToInt(OrderItem::getRating)
                .average()
                .orElse(5.0);

        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setRatingScore(BigDecimal.valueOf(Math.round(avg * 10) / 10.0));
            productMapper.updateById(product);
        }
    }

    private BigDecimal applyCoupon(Long userId, Long userCouponId, BigDecimal totalAmount) {
        if (userCouponId == null) return BigDecimal.ZERO;

        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null) throw new RuntimeException("优惠券不存在");
        if (!uc.getUserId().equals(userId)) throw new RuntimeException("优惠券不属于当前用户");
        if (uc.getStatus() != 0) throw new RuntimeException("优惠券已使用或已过期");

        Coupon coupon = couponMapper.selectById(uc.getCouponId());
        if (coupon == null) throw new RuntimeException("优惠券模板不存在");

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new RuntimeException("优惠券不在有效期内");
        }

        BigDecimal discount;
        if (coupon.getType() == 2) {
            if (coupon.getMinAmount() != null && totalAmount.compareTo(coupon.getMinAmount()) < 0) {
                throw new RuntimeException("未达到优惠券使用门槛");
            }
            discount = totalAmount.multiply(BigDecimal.TEN.subtract(coupon.getValue()))
                .divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
        } else {
            if (coupon.getMinAmount() != null && totalAmount.compareTo(coupon.getMinAmount()) < 0) {
                throw new RuntimeException("未达到优惠券使用门槛");
            }
            discount = coupon.getValue();
        }
        return discount;
    }

    private void markCouponUsed(Long userCouponId, Long orderId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc != null) {
            uc.setStatus(1);
            uc.setOrderId(orderId);
            uc.setUseTime(LocalDateTime.now());
            userCouponMapper.updateById(uc);
        }
    }

    private OrderVO toVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        vo.setItems(items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList()));

        return vo;
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "SP" + date + uuid;
    }
}