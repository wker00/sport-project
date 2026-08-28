package com.sportzone.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportzone.admin.dto.ReviewReplyDTO;
import com.sportzone.admin.service.AdminService;
import com.sportzone.common.annotation.OperateLog;
import com.sportzone.admin.vo.AdminReviewVO;
import com.sportzone.user.entity.Order;
import com.sportzone.user.entity.OrderItem;
import com.sportzone.user.entity.Product;
import com.sportzone.user.entity.User;
import com.sportzone.user.mapper.OrderItemMapper;
import com.sportzone.user.mapper.OrderMapper;
import com.sportzone.user.mapper.ProductMapper;
import com.sportzone.user.mapper.UserMapper;
import com.sportzone.utils.Result;
import com.sportzone.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/review")
@Tag(name = "评价管理", description = "管理员评价管理")
public class AdminReviewController {

    private final AdminService adminService;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    public AdminReviewController(AdminService adminService,
                                 OrderItemMapper orderItemMapper,
                                 OrderMapper orderMapper,
                                 UserMapper userMapper,
                                 ProductMapper productMapper) {
        this.adminService = adminService;
        this.orderItemMapper = orderItemMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
    }

    @GetMapping("/list")
    @Operation(summary = "评价列表", description = "可按商品ID、评分筛选")
    public Result<List<AdminReviewVO>> listReviews(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer rating) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(OrderItem::getRating);
        if (productId != null) {
            wrapper.eq(OrderItem::getProductId, productId);
        }
        if (rating != null) {
            wrapper.eq(OrderItem::getRating, rating);
        }
        wrapper.orderByDesc(OrderItem::getReviewTime);

        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        if (items.isEmpty()) {
            return Result.success(List.of());
        }

        List<Long> orderIds = items.stream()
                .map(OrderItem::getOrderId)
                .distinct()
                .collect(Collectors.toList());
        List<Order> orders = orderMapper.selectBatchIds(orderIds);
        Map<Long, Order> orderMap = orders.stream()
                .collect(Collectors.toMap(Order::getId, o -> o));

        List<Long> userIds = orders.stream()
                .map(Order::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<Long> productIds = items.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .collect(Collectors.toList());
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<AdminReviewVO> result = items.stream().map(item -> {
            AdminReviewVO vo = new AdminReviewVO();
            vo.setId(item.getId());
            vo.setProductId(item.getProductId());
            vo.setProductName(item.getProductName());
            vo.setProductImage(item.getProductImage());
            vo.setRating(item.getRating());
            vo.setReviewContent(item.getReviewContent());
            vo.setReviewImages(item.getReviewImages());
            vo.setReviewTime(item.getReviewTime());
            vo.setReplyContent(item.getReplyContent());
            vo.setReplyTime(item.getReplyTime());

            Order order = orderMap.get(item.getOrderId());
            if (order != null) {
                vo.setOrderId(order.getId());
                vo.setOrderNo(order.getOrderNo());
                User user = userMap.get(order.getUserId());
                if (user != null) {
                    vo.setUserId(user.getId());
                    vo.setUsername(user.getUsername());
                    vo.setNickname(user.getNickname());
                    vo.setAvatar(user.getAvatar());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "评价详情")
    public Result<AdminReviewVO> getReviewDetail(@PathVariable Long id) {
        OrderItem item = orderItemMapper.selectById(id);
        if (item == null || item.getRating() == null) {
            return Result.error("评价不存在");
        }

        AdminReviewVO vo = new AdminReviewVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setProductImage(item.getProductImage());
        vo.setRating(item.getRating());
        vo.setReviewContent(item.getReviewContent());
        vo.setReviewImages(item.getReviewImages());
        vo.setReviewTime(item.getReviewTime());
        vo.setReplyContent(item.getReplyContent());
        vo.setReplyTime(item.getReplyTime());

        Order order = orderMapper.selectById(item.getOrderId());
        if (order != null) {
            vo.setOrderId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                vo.setUserId(user.getId());
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
        }
        return Result.success(vo);
    }

    @PutMapping("/{id}/reply")
    @Operation(summary = "回复评价")
    @OperateLog(module = "review", type = "reply", description = "回复评价「#id」")
    public Result<Void> replyReview(@PathVariable Long id, @RequestBody @Valid ReviewReplyDTO dto) {
        adminService.replyReview(ThreadLocalUtil.getUserId(), id, dto);
        return Result.success("回复成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价", description = "清空评价内容，保留订单商品记录")
    @OperateLog(module = "review", type = "delete", description = "删除评价「#id」")
    public Result<Void> deleteReview(@PathVariable Long id) {
        adminService.deleteReview(ThreadLocalUtil.getUserId(), id);
        return Result.success("删除成功");
    }
}
