package com.sportzone.user.controller;

import com.sportzone.user.dto.ClaimCouponDTO;
import com.sportzone.user.service.CouponService;
import com.sportzone.user.vo.CouponVO;
import com.sportzone.user.vo.UserCouponVO;
import com.sportzone.utils.Result;
import com.sportzone.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/coupon")
@Tag(name = "优惠券管理", description = "优惠券领取、查询")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/list")
    @Operation(summary = "可领取优惠券列表", description = "获取当前可领取的全部优惠券")
    public Result<List<CouponVO>> getAvailableCouponList() {
        return Result.success(couponService.getAvailableCouponList());
    }

    @GetMapping("/my")
    @Operation(summary = "我的优惠券", description = "获取当前用户已领取的优惠券")
    public Result<List<UserCouponVO>> getMyCoupons() {
        return Result.success(couponService.getMyCoupons(ThreadLocalUtil.getUserId()));
    }

    @PostMapping("/claim")
    @Operation(summary = "领取优惠券", description = "使用积分领取优惠券")
    public Result<UserCouponVO> claimCoupon(@RequestBody @Valid ClaimCouponDTO dto) {
        return Result.success(couponService.claimCoupon(ThreadLocalUtil.getUserId(), dto));
    }
}
