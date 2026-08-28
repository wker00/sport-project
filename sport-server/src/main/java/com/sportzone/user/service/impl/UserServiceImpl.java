package com.sportzone.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportzone.user.entity.User;
import com.sportzone.user.mapper.UserMapper;
import com.sportzone.user.mapper.OrderMapper;
import com.sportzone.user.service.UserService;
import com.sportzone.user.dto.ChangePasswordDTO;
import com.sportzone.user.dto.LoginDTO;
import com.sportzone.user.dto.RegisterDTO;
import com.sportzone.user.dto.UpdateUserDTO;
import com.sportzone.utils.JwtUtils;
import com.sportzone.utils.PasswordEncoder;
import com.sportzone.user.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    public UserServiceImpl(UserMapper userMapper, OrderMapper orderMapper, JwtUtils jwtUtils, PasswordEncoder passwordEncoder, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("账号已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setUserLevel(1);
        user.setPointsBalance(0L);
        user.setNickname("用户" + String.format("%04d", (int)(Math.random() * 10000)));
        userMapper.insert(user);
    }

    @Override
    public String login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }

        // 生成token
        String token = jwtUtils.genToken(Map.of("id", user.getId(), "username", user.getUsername()));
        // 保存token到redis
        stringRedisTemplate.opsForValue().set("user_token" + user.getId(), token, 1, TimeUnit.DAYS);
        return token;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void updateUserInfo(Long userId, UpdateUserDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (StringUtils.hasText(dto.getNickname())) {
            if (dto.getNickname().length() < 3) {
                throw new RuntimeException("昵称长度最少3个字符");
            }
            user.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getEmail())) {
            user.setEmail(dto.getEmail());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            user.setPhone(dto.getPhone());
        }

        userMapper.updateById(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("两次输入的新密码不一致");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
        stringRedisTemplate.delete("user_token" + userId);
    }

    @Override
    public void logout(Long userId) {
        stringRedisTemplate.delete("user_token" + userId);
    }

    @Override
    public void upgradeLevel(Long userId) {
        BigDecimal totalSpent = orderMapper.selectTotalSpent(userId);
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        User user = userMapper.selectById(userId);
        if (user == null) return;

        user.setTotalConsumption(totalSpent);

        int newLevel;
        double amount = totalSpent.doubleValue();
        if (amount >= 67000) {
            newLevel = 5;
        } else if (amount >= 17000) {
            newLevel = 4;
        } else if (amount >= 7000) {
            newLevel = 3;
        } else if (amount >= 2000) {
            newLevel = 2;
        } else {
            newLevel = 1;
        }

        if (newLevel > user.getUserLevel()) {
            user.setUserLevel(newLevel);
        }
        userMapper.updateById(user);
    }
}