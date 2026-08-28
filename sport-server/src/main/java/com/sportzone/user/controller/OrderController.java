package com.sportzone.user.controller;

import com.sportzone.user.dto.BuyNowDTO;
import com.sportzone.user.dto.CreateOrderDTO;
import com.sportzone.user.dto.RefundDTO;
import com.sportzone.user.dto.SubmitReviewDTO;
import com.sportzone.user.service.OrderService;
import com.sportzone.utils.Result;
import com.sportzone.utils.ThreadLocalUtil;
import com.sportzone.user.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/order")
@Tag(name = "订单管理", description = "订单创建、查询、取消")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "创建订单", description = "从购物车创建订单")
    public Result<OrderVO> createOrder(@RequestBody @Valid CreateOrderDTO dto) {
        return Result.success(orderService.createOrder(ThreadLocalUtil.getUserId(), dto));
    }

    @PostMapping("/buyNow")
    @Operation(summary = "立即购买", description = "直接购买商品，跳过购物车")
    public Result<OrderVO> buyNow(@RequestBody @Valid BuyNowDTO dto) {
        return Result.success(orderService.buyNow(ThreadLocalUtil.getUserId(), dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情", description = "获取订单详细信息")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(ThreadLocalUtil.getUserId(), id));
    }

    @GetMapping
    @Operation(summary = "订单列表", description = "获取当前用户的所有订单")
    public Result<List<OrderVO>> getOrderList() {
        return Result.success(orderService.getOrderList(ThreadLocalUtil.getUserId()));
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "模拟支付", description = "模拟支付，将待付款订单变为待发货")
    public Result<Void> payOrder(@PathVariable Long id) {
        orderService.payOrder(ThreadLocalUtil.getUserId(), id);
        return Result.success("支付成功");
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消待付款订单，恢复库存")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(ThreadLocalUtil.getUserId(), id);
        return Result.success("订单已取消");
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认收货", description = "确认收货完成订单")
    public Result<Void> confirmReceipt(@PathVariable Long id) {
        orderService.confirmReceipt(ThreadLocalUtil.getUserId(), id);
        return Result.success("已确认收货");
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "提交评价", description = "对订单中的商品进行评价")
    public Result<Void> submitReview(@PathVariable Long id, @RequestBody @Valid SubmitReviewDTO dto) {
        orderService.submitReview(ThreadLocalUtil.getUserId(), id, dto);
        return Result.success("评价成功");
    }

    @PutMapping("/{id}/refund")
    @Operation(summary = "申请退款", description = "对已付款且未完成的订单申请退款")
    public Result<Void> applyRefund(@PathVariable Long id, @RequestBody @Valid RefundDTO dto) {
        orderService.applyRefund(ThreadLocalUtil.getUserId(), id, dto);
        return Result.success("退款申请已提交");
    }
}