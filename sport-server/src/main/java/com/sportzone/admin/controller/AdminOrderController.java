package com.sportzone.admin.controller;

import com.sportzone.admin.dto.OrderRefundDTO;
import com.sportzone.admin.dto.OrderShipDTO;
import com.sportzone.admin.service.AdminService;
import com.sportzone.common.annotation.OperateLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportzone.user.entity.Order;
import com.sportzone.user.entity.OrderItem;
import com.sportzone.user.entity.User;
import com.sportzone.user.entity.UserAddress;
import com.sportzone.user.mapper.OrderItemMapper;
import com.sportzone.user.mapper.OrderMapper;
import com.sportzone.user.mapper.UserAddressMapper;
import com.sportzone.user.mapper.UserMapper;
import com.sportzone.user.vo.AddressVO;
import com.sportzone.user.vo.OrderItemVO;
import com.sportzone.user.vo.OrderVO;
import com.sportzone.user.vo.UserVO;
import com.sportzone.utils.Result;
import com.sportzone.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/order")
@Tag(name = "订单管理", description = "管理员订单管理")
public class AdminOrderController {

    private final AdminService adminService;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final UserAddressMapper userAddressMapper;

    public AdminOrderController(AdminService adminService, OrderMapper orderMapper,
                                OrderItemMapper orderItemMapper, UserMapper userMapper,
                                UserAddressMapper userAddressMapper) {
        this.adminService = adminService;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userMapper = userMapper;
        this.userAddressMapper = userAddressMapper;
    }

    @GetMapping("/list")
    @Operation(summary = "订单列表", description = "可按状态筛选")
    public Result<List<OrderVO>> listOrders(@RequestParam(required = false) Integer status) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        List<OrderVO> list = orderMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(toVO(order));
    }

    @PutMapping("/{id}/deliver")
    @Operation(summary = "确认送达")
    @OperateLog(module = "order", type = "deliver", description = "确认送达订单「#id」")
    public Result<Void> deliverOrder(@PathVariable Long id) {
        adminService.deliverOrder(ThreadLocalUtil.getUserId(), id);
        return Result.success("已确认送达");
    }

    @PutMapping("/ship")
    @Operation(summary = "发货")
    @OperateLog(module = "order", type = "ship", description = "订单发货")
    public Result<Void> shipOrder(@RequestBody @Valid OrderShipDTO dto) {
        adminService.shipOrder(ThreadLocalUtil.getUserId(), dto);
        return Result.success("发货成功");
    }

    @PutMapping("/refund")
    @Operation(summary = "处理退款")
    @OperateLog(module = "order", type = "refund", description = "处理退款")
    public Result<Void> processRefund(@RequestBody @Valid OrderRefundDTO dto) {
        adminService.processRefund(ThreadLocalUtil.getUserId(), dto);
        return Result.success(dto.getRefundStatus() == 2 ? "退款已通过" : "退款已拒绝");
    }

    private OrderVO toVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        if (order.getUserId() != null) {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                vo.setUser(userVO);
            }
        }
        if (order.getAddressId() != null) {
            UserAddress addr = userAddressMapper.selectById(order.getAddressId());
            if (addr != null) {
                AddressVO addressVO = new AddressVO();
                BeanUtils.copyProperties(addr, addressVO);
                vo.setAddress(addressVO);
            }
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        vo.setItems(items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList()));
        return vo;
    }
}
