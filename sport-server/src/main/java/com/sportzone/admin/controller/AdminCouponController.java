package com.sportzone.admin.controller;

import com.sportzone.admin.dto.AdminCreateCouponDTO;
import com.sportzone.admin.dto.AdminUpdateCouponDTO;
import com.sportzone.common.annotation.OperateLog;
import com.sportzone.user.service.CouponService;
import com.sportzone.user.vo.CouponRecordVO;
import com.sportzone.user.vo.CouponVO;
import com.sportzone.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupon")
@Tag(name = "优惠券管理", description = "管理员优惠券管理")
public class AdminCouponController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/list")
    @Operation(summary = "优惠券模板列表")
    public Result<List<CouponVO>> getCouponList() {
        return Result.success(couponService.getAdminCouponList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "优惠券模板详情")
    public Result<CouponVO> getCouponById(@PathVariable Long id) {
        return Result.success(couponService.getAdminCouponById(id));
    }

    @PostMapping
    @Operation(summary = "创建优惠券")
    @OperateLog(module = "coupon", type = "create", description = "创建优惠券「#dto.name」")
    public Result<Void> createCoupon(@RequestBody @Valid AdminCreateCouponDTO dto) {
        couponService.createAdminCoupon(dto);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新优惠券")
    @OperateLog(module = "coupon", type = "update", description = "更新优惠券「#id」")
    public Result<Void> updateCoupon(@PathVariable Long id, @RequestBody @Valid AdminUpdateCouponDTO dto) {
        couponService.updateAdminCoupon(id, dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除优惠券")
    @OperateLog(module = "coupon", type = "delete", description = "删除优惠券「#id」")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteAdminCoupon(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}/records")
    @Operation(summary = "优惠券发放记录")
    public Result<List<CouponRecordVO>> getCouponRecords(@PathVariable Long id) {
        return Result.success(couponService.getCouponRecords(id));
    }
}
