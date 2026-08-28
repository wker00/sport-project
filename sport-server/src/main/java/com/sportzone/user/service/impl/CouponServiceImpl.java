package com.sportzone.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportzone.admin.dto.AdminCreateCouponDTO;
import com.sportzone.admin.dto.AdminUpdateCouponDTO;
import com.sportzone.user.dto.ClaimCouponDTO;
import com.sportzone.user.entity.Coupon;
import com.sportzone.user.entity.User;
import com.sportzone.user.entity.UserCoupon;
import com.sportzone.user.mapper.CouponMapper;
import com.sportzone.user.mapper.UserCouponMapper;
import com.sportzone.user.mapper.UserMapper;
import com.sportzone.user.service.CouponService;
import com.sportzone.user.vo.CouponRecordVO;
import com.sportzone.user.vo.CouponVO;
import com.sportzone.user.vo.UserCouponVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final UserMapper userMapper;

    public CouponServiceImpl(CouponMapper couponMapper, UserCouponMapper userCouponMapper,
                             UserMapper userMapper) {
        this.couponMapper = couponMapper;
        this.userCouponMapper = userCouponMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<CouponVO> getAvailableCouponList() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Coupon::getStock, 0);
        wrapper.le(Coupon::getStartTime, now);
        wrapper.ge(Coupon::getEndTime, now);
        wrapper.orderByAsc(Coupon::getPointsCost);
        return couponMapper.selectList(wrapper).stream()
                .map(this::toCouponVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserCouponVO> getMyCoupons(Long userId) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        wrapper.orderByDesc(UserCoupon::getCreateTime);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);
        return userCoupons.stream()
                .map(uc -> {
                    Coupon coupon = couponMapper.selectById(uc.getCouponId());
                    if (coupon == null) {
                        return null;
                    }
                    return toUserCouponVO(uc, coupon);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserCouponVO claimCoupon(Long userId, ClaimCouponDTO dto) {
        Coupon coupon = couponMapper.selectById(dto.getCouponId());
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new RuntimeException("优惠券不在领取时间范围内");
        }

        if (coupon.getStock() <= 0) {
            throw new RuntimeException("优惠券库存不足");
        }

        LambdaQueryWrapper<UserCoupon> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserCoupon::getUserId, userId);
        checkWrapper.eq(UserCoupon::getCouponId, dto.getCouponId());
        if (userCouponMapper.selectCount(checkWrapper) > 0) {
            throw new RuntimeException("已领取过该优惠券");
        }

        if (coupon.getPointsCost() > 0) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            if (user.getPointsBalance() < coupon.getPointsCost()) {
                throw new RuntimeException("积分不足，需要积分：" + coupon.getPointsCost());
            }
            user.setPointsBalance(user.getPointsBalance() - coupon.getPointsCost());
            userMapper.updateById(user);
        }

        coupon.setStock(coupon.getStock() - 1);
        couponMapper.updateById(coupon);

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(coupon.getId());
        userCoupon.setStatus(0);
        userCouponMapper.insert(userCoupon);

        return toUserCouponVO(userCoupon, coupon);
    }

    @Override
    public List<CouponVO> getAdminCouponList() {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Coupon::getCreateTime);
        return couponMapper.selectList(wrapper).stream()
                .map(this::toCouponVO)
                .collect(Collectors.toList());
    }

    @Override
    public CouponVO getAdminCouponById(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        return toCouponVO(coupon);
    }

    @Override
    public void createAdminCoupon(AdminCreateCouponDTO dto) {
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        couponMapper.insert(coupon);
    }

    @Override
    public void updateAdminCoupon(Long id, AdminUpdateCouponDTO dto) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        if (StringUtils.hasText(dto.getName())) coupon.setName(dto.getName());
        if (dto.getType() != null) coupon.setType(dto.getType());
        if (dto.getValue() != null) coupon.setValue(dto.getValue());
        if (dto.getMinAmount() != null) coupon.setMinAmount(dto.getMinAmount());
        if (dto.getPointsCost() != null) coupon.setPointsCost(dto.getPointsCost());
        if (dto.getStock() != null) coupon.setStock(dto.getStock());
        if (dto.getStartTime() != null) coupon.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) coupon.setEndTime(dto.getEndTime());
        couponMapper.updateById(coupon);
    }

    @Override
    public void deleteAdminCoupon(Long id) {
        if (couponMapper.selectById(id) == null) {
            throw new RuntimeException("优惠券不存在");
        }
        couponMapper.deleteById(id);
    }

    @Override
    public List<CouponRecordVO> getCouponRecords(Long couponId) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getCouponId, couponId);
        wrapper.orderByDesc(UserCoupon::getCreateTime);
        List<UserCoupon> records = userCouponMapper.selectList(wrapper);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream().map(uc -> {
            CouponRecordVO vo = new CouponRecordVO();
            BeanUtils.copyProperties(uc, vo);
            vo.setCouponId(couponId);
            User user = userMapper.selectById(uc.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private CouponVO toCouponVO(Coupon coupon) {
        CouponVO vo = new CouponVO();
        BeanUtils.copyProperties(coupon, vo);
        return vo;
    }

    private UserCouponVO toUserCouponVO(UserCoupon userCoupon, Coupon coupon) {
        UserCouponVO vo = new UserCouponVO();
        BeanUtils.copyProperties(userCoupon, vo);
        if (coupon != null) {
            BeanUtils.copyProperties(coupon, vo);
        }
        vo.setId(userCoupon.getId());
        vo.setCouponId(userCoupon.getCouponId());
        return vo;
    }
}
