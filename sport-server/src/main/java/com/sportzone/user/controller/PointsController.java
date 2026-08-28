package com.sportzone.user.controller;

import com.sportzone.user.dto.ExchangeDTO;
import com.sportzone.user.service.PointsService;
import com.sportzone.user.vo.PointsExchangeOrderVO;
import com.sportzone.user.vo.PointsGiftVO;
import com.sportzone.user.vo.PointsRecordVO;
import com.sportzone.user.vo.SigninStatusVO;
import com.sportzone.user.vo.SigninVO;
import com.sportzone.utils.Result;
import com.sportzone.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/points")
@Tag(name = "积分管理", description = "积分查询、积分商品兑换")
public class PointsController {

    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @GetMapping("/balance")
    @Operation(summary = "获取积分余额", description = "获取当前用户的积分余额")
    public Result<Long> getPointsBalance() {
        return Result.success(pointsService.getPointsBalance(ThreadLocalUtil.getUserId()));
    }

    @GetMapping("/records")
    @Operation(summary = "积分记录", description = "获取当前用户的积分变动记录")
    public Result<List<PointsRecordVO>> getPointsRecordList() {
        return Result.success(pointsService.getPointsRecordList(ThreadLocalUtil.getUserId()));
    }

    @GetMapping("/gifts")
    @Operation(summary = "积分商品列表", description = "获取可兑换的积分商品")
    public Result<List<PointsGiftVO>> getGiftList() {
        return Result.success(pointsService.getGiftList());
    }

    @PostMapping("/exchange")
    @Operation(summary = "兑换积分商品", description = "使用积分兑换商品")
    public Result<PointsExchangeOrderVO> exchange(@RequestBody @Valid ExchangeDTO dto) {
        return Result.success(pointsService.exchange(ThreadLocalUtil.getUserId(), dto));
    }

    @GetMapping("/exchange/orders")
    @Operation(summary = "兑换记录", description = "获取当前用户的积分兑换记录")
    public Result<List<PointsExchangeOrderVO>> getExchangeOrderList() {
        return Result.success(pointsService.getExchangeOrderList(ThreadLocalUtil.getUserId()));
    }

    @PutMapping("/exchange/{id}/confirm")
    @Operation(summary = "确认收货", description = "确认兑换订单收货")
    public Result<Void> confirmExchangeOrder(@PathVariable Long id) {
        pointsService.confirmExchangeOrder(ThreadLocalUtil.getUserId(), id);
        return Result.success("确认收货成功");
    }

    @GetMapping("/signin/status")
    @Operation(summary = "签到状态", description = "获取当日签到状态和连续天数")
    public Result<SigninStatusVO> signinStatus() {
        return Result.success(pointsService.signinStatus(ThreadLocalUtil.getUserId()));
    }

    @PostMapping("/signin")
    @Operation(summary = "每日签到", description = "用户每日签到获取积分")
    public Result<SigninVO> signin() {
        return Result.success(pointsService.signin(ThreadLocalUtil.getUserId()));
    }
}