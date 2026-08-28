package com.sportzone.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sportzone.admin.dto.AdminLoginDTO;
import com.sportzone.admin.dto.AdminRegisterDTO;
import com.sportzone.admin.dto.AdminUpdateDTO;
import com.sportzone.admin.dto.AdminChangePasswordDTO;
import com.sportzone.admin.dto.OrderRefundDTO;
import com.sportzone.admin.dto.OrderShipDTO;
import com.sportzone.admin.dto.ReviewReplyDTO;
import com.sportzone.admin.entity.Admin;
import com.sportzone.admin.mapper.AdminMapper;
import com.sportzone.admin.service.AdminService;
import com.sportzone.admin.vo.AdminVO;
import com.sportzone.admin.vo.DashboardVO;
import com.sportzone.user.entity.Order;
import com.sportzone.user.entity.OrderItem;
import com.sportzone.user.entity.Product;
import com.sportzone.user.entity.User;
import com.sportzone.user.mapper.OrderItemMapper;
import com.sportzone.user.mapper.OrderMapper;
import com.sportzone.user.mapper.ProductMapper;
import com.sportzone.user.mapper.UserMapper;
import com.sportzone.utils.JwtUtils;
import com.sportzone.utils.PasswordEncoder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    public AdminServiceImpl(AdminMapper adminMapper,
                            UserMapper userMapper, ProductMapper productMapper,
                            OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            JwtUtils jwtUtils,
                            PasswordEncoder passwordEncoder,
                            StringRedisTemplate stringRedisTemplate) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String login(AdminLoginDTO dto, String ip) {
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, dto.getUsername())
                        .eq(Admin::getStatus, 1));
        if (admin == null) {
            throw new RuntimeException("账号不存在或已禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(ip);
        adminMapper.updateById(admin);

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", admin.getId());
        claims.put("username", admin.getUsername());
        claims.put("role", "admin");
        String token = jwtUtils.genToken(claims);

        stringRedisTemplate.opsForValue().set(
                "admin_token" + admin.getId(),
                token,
                24, java.util.concurrent.TimeUnit.HOURS);

        return token;
    }

    @Override
    public void logout(Long adminId) {
        stringRedisTemplate.delete("admin_token" + adminId);
    }

    @Override
    public void register(Long currentAdminId, AdminRegisterDTO dto) {
        requireSuperAdmin(currentAdminId);

        Long count = adminMapper.selectCount(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, dto.getUsername())
                        .eq(Admin::getIsDeleted, 0));
        if (count > 0) {
            throw new RuntimeException("账号已存在");
        }

        Admin admin = new Admin();
        BeanUtils.copyProperties(dto, admin);
        if (admin.getNickname() == null || admin.getNickname().trim().isEmpty()) {
            admin.setNickname("管理员" + String.format("%04d", (int)(Math.random() * 10000)));
        }
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setStatus(1);
        admin.setRole(dto.getRole() != null ? dto.getRole() : 2);
        adminMapper.insert(admin);
    }

    @Override
    public AdminVO getAdminInfo(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        return toVO(admin);
    }

    @Override
    public void updateAdminInfo(Long adminId, AdminUpdateDTO dto) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        admin.setNickname(dto.getNickname());
        admin.setPhone(dto.getPhone());
        admin.setEmail(dto.getEmail());
        adminMapper.updateById(admin);
    }

    @Override
    public void updateAdminInfoById(Long currentAdminId, Long targetId, AdminUpdateDTO dto) {
        requireSuperAdmin(currentAdminId);
        Admin admin = adminMapper.selectById(targetId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        if (dto.getRole() != null) {
            if (currentAdminId.equals(targetId)) {
                throw new RuntimeException("不能修改自己的角色");
            }
            if (admin.getRole() != null && admin.getRole() == 1) {
                throw new RuntimeException("不能修改其他超级管理员的角色");
            }
            if (dto.getRole() < 1 || dto.getRole() > 2) {
                throw new RuntimeException("角色值无效");
            }
            admin.setRole(dto.getRole());
        }
        if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
            if (dto.getNickname().length() < 3 || dto.getNickname().length() > 20) {
                throw new RuntimeException("昵称长度必须在3到20个字符之间");
            }
            admin.setNickname(dto.getNickname());
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            if (!dto.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new RuntimeException("手机号格式不正确");
            }
            admin.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            if (!dto.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                throw new RuntimeException("邮箱格式不正确");
            }
            admin.setEmail(dto.getEmail());
        }
        adminMapper.updateById(admin);
    }

    @Override
    public void updateAvatar(Long adminId, String avatarUrl) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        admin.setAvatar(avatarUrl);
        adminMapper.updateById(admin);
    }

    @Override
    public List<AdminVO> listAdmins() {
        return adminMapper.selectList(null).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAdmin(Long currentAdminId, Long targetAdminId) {
        requireSuperAdmin(currentAdminId);
        if (currentAdminId.equals(targetAdminId)) {
            throw new RuntimeException("不能删除当前登录的管理员账号");
        }
        Admin admin = adminMapper.selectById(targetAdminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        adminMapper.deleteById(targetAdminId);
        stringRedisTemplate.delete("admin_token" + targetAdminId);
    }

    @Override
    public void enableAdmin(Long currentAdminId, Long id) {
        requireSuperAdmin(currentAdminId);
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        admin.setStatus(1);
        adminMapper.updateById(admin);
    }

    @Override
    public void disableAdmin(Long currentAdminId, Long targetAdminId) {
        requireSuperAdmin(currentAdminId);
        if (currentAdminId.equals(targetAdminId)) {
            throw new RuntimeException("不能禁用当前登录的管理员账号");
        }
        Admin admin = adminMapper.selectById(targetAdminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        admin.setStatus(0);
        adminMapper.updateById(admin);
        stringRedisTemplate.delete("admin_token" + targetAdminId);
    }

    @Override
    public void resetAdminPassword(Long currentAdminId, Long targetAdminId) {
        requireSuperAdmin(currentAdminId);
        if (currentAdminId.equals(targetAdminId)) {
            throw new RuntimeException("不能重置自己的密码");
        }
        Admin admin = adminMapper.selectById(targetAdminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        admin.setPassword(passwordEncoder.encode("123456"));
        adminMapper.updateById(admin);
        stringRedisTemplate.delete("admin_token" + targetAdminId);
    }

    @Override
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();

        vo.setTotalUsers(userMapper.selectCount(null));
        vo.setTotalProducts(productMapper.selectCount(null));
        vo.setTotalOrders(orderMapper.selectCount(null));

        Long pendingShip = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1));
        vo.setPendingShipOrders(pendingShip);

        Long refundRequests = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getRefundStatus, 1));
        vo.setRefundRequests(refundRequests);

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<Order> todayOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .between(Order::getCreateTime, todayStart, todayEnd));
        BigDecimal todayAmount = todayOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTodayOrderAmount(todayAmount);

        return vo;
    }

    @Override
    @Transactional
    public void deliverOrder(Long adminId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new RuntimeException("当前订单状态不允许确认送达");
        }
        order.setStatus(3);
        order.setOperatorId(adminId);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void shipOrder(Long adminId, OrderShipDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new RuntimeException("当前订单状态不允许发货");
        }

        order.setExpressCompany(dto.getExpressCompany());
        order.setExpressNo(dto.getExpressNo());
        order.setDeliveryTime(LocalDateTime.now());
        order.setStatus(2);
        order.setOperatorId(adminId);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void processRefund(Long adminId, OrderRefundDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getRefundStatus() != 1) {
            throw new RuntimeException("当前订单没有退款申请");
        }

        if (dto.getRefundStatus() == 2) {
            order.setStatus(6);
            // 恢复库存
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            for (OrderItem item : items) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    product.setSalesCount(Math.max(0, product.getSalesCount() - item.getQuantity()));
                    productMapper.updateById(product);
                }
            }
            // 重算累计消费（仅当订单已计入时才会扣减）
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                BigDecimal total = orderMapper.selectTotalSpent(user.getId());
                user.setTotalConsumption(total);
                userMapper.updateById(user);
            }
        }
        order.setRefundStatus(dto.getRefundStatus());
        if (dto.getRefundReason() != null) {
            order.setRefundReason(dto.getRefundReason());
        }
        order.setOperatorId(adminId);
        orderMapper.updateById(order);
    }

    @Override
    public void changePassword(Long adminId, AdminChangePasswordDTO dto) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), admin.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("两次输入的新密码不一致");
        }

        admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        adminMapper.updateById(admin);
        stringRedisTemplate.delete("admin_token" + adminId);
    }

    private void requireSuperAdmin(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null || admin.getRole() == null || admin.getRole() != 1) {
            throw new RuntimeException("无权限，仅超级管理员可执行此操作");
        }
    }

    private AdminVO toVO(Admin admin) {
        AdminVO vo = new AdminVO();
        BeanUtils.copyProperties(admin, vo);
        return vo;
    }

    @Override
    public void replyReview(Long adminId, Long reviewId, ReviewReplyDTO dto) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        OrderItem item = orderItemMapper.selectById(reviewId);
        if (item == null) {
            throw new RuntimeException("评价不存在");
        }
        if (item.getRating() == null) {
            throw new RuntimeException("该商品未评价");
        }
        item.setReplyContent(dto.getReplyContent());
        item.setReplyTime(LocalDateTime.now());
        orderItemMapper.updateById(item);
    }

    @Override
    public void deleteReview(Long adminId, Long reviewId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        OrderItem item = orderItemMapper.selectById(reviewId);
        if (item == null) {
            throw new RuntimeException("评价不存在");
        }
        if (item.getRating() == null) {
            throw new RuntimeException("该商品未评价");
        }
        orderItemMapper.update(null,
                new LambdaUpdateWrapper<OrderItem>()
                        .eq(OrderItem::getId, reviewId)
                        .set(OrderItem::getRating, null)
                        .set(OrderItem::getReviewContent, null)
                        .set(OrderItem::getReviewImages, null)
                        .set(OrderItem::getReviewTime, null)
                        .set(OrderItem::getReplyContent, null)
                        .set(OrderItem::getReplyTime, null));
    }
}
