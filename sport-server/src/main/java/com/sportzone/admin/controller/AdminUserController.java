package com.sportzone.admin.controller;

import com.sportzone.admin.entity.Admin;
import com.sportzone.admin.mapper.AdminMapper;
import com.sportzone.common.annotation.OperateLog;
import com.sportzone.user.entity.User;
import com.sportzone.user.mapper.UserMapper;
import com.sportzone.user.vo.UserVO;
import com.sportzone.utils.PasswordEncoder;
import com.sportzone.utils.Result;
import com.sportzone.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/user")
@Tag(name = "用户管理", description = "管理员管理用户")
public class AdminUserController {

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    public AdminUserController(UserMapper userMapper, AdminMapper adminMapper,
                                PasswordEncoder passwordEncoder,
                                StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @GetMapping("/list")
    @Operation(summary = "用户列表", description = "返回所有用户信息，含累计消费金额")
    public Result<List<UserVO>> listUsers() {
        List<UserVO> list = userMapper.selectList(null).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情", description = "获取用户详细信息，含累计消费金额")
    public Result<UserVO> getUserDetail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(toVO(user));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "重置用户密码", description = "重置为默认密码 123456（仅超级管理员可用）")
    @OperateLog(module = "user", type = "reset_password", description = "重置用户「#id」密码")
    public Result<Void> resetPassword(@PathVariable Long id) {
        Admin admin = adminMapper.selectById(ThreadLocalUtil.getUserId());
        if (admin == null || admin.getRole() == null || admin.getRole() != 1) {
            return Result.error("无权限，仅超级管理员可执行此操作");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
        stringRedisTemplate.delete("user_token" + id);
        return Result.success("重置成功");
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
