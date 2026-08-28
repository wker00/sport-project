package com.sportzone.admin.controller;

import com.sportzone.admin.dto.CreatePointsGiftDTO;
import com.sportzone.admin.dto.ShipExchangeOrderDTO;
import com.sportzone.admin.dto.UpdatePointsGiftDTO;
import com.sportzone.common.annotation.OperateLog;
import com.sportzone.user.service.OssService;
import com.sportzone.user.service.PointsService;
import com.sportzone.user.vo.ExchangeOrderVO;
import com.sportzone.user.vo.PointsGiftVO;
import com.sportzone.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/points/gifts")
@Tag(name = "积分商品管理", description = "管理员积分商品管理")
public class AdminPointsGiftController {

    private final PointsService pointsService;
    private final OssService ossService;

    public AdminPointsGiftController(PointsService pointsService, OssService ossService) {
        this.pointsService = pointsService;
        this.ossService = ossService;
    }

    @GetMapping("/list")
    @Operation(summary = "积分商品列表")
    public Result<List<PointsGiftVO>> getGiftList() {
        return Result.success(pointsService.getAdminGiftList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "积分商品详情")
    public Result<PointsGiftVO> getGiftById(@PathVariable Long id) {
        return Result.success(pointsService.getAdminGiftById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "创建积分商品（JSON）")
    @OperateLog(module = "points_gift", type = "create", description = "创建积分商品")
    public Result<Void> createGift(@RequestBody @Valid CreatePointsGiftDTO dto) {
        pointsService.createAdminGift(dto);
        return Result.success("创建成功");
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建积分商品（带图片上传）")
    @OperateLog(module = "points_gift", type = "create", description = "创建积分商品")
    public Result<Void> createGiftWithImage(
            @RequestPart("gift") @Valid CreatePointsGiftDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            dto.setImage(ossService.uploadProductImage(file));
        }
        pointsService.createAdminGift(dto);
        return Result.success("创建成功");
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "更新积分商品（JSON）")
    @OperateLog(module = "points_gift", type = "update", description = "更新积分商品「#id」")
    public Result<Void> updateGift(@PathVariable Long id, @RequestBody @Valid UpdatePointsGiftDTO dto) {
        pointsService.updateAdminGift(id, dto);
        return Result.success("更新成功");
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "更新积分商品（带图片上传）")
    @OperateLog(module = "points_gift", type = "update", description = "更新积分商品「#id」")
    public Result<Void> updateGiftWithImage(
            @PathVariable Long id,
            @RequestPart("gift") @Valid UpdatePointsGiftDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            PointsGiftVO old = pointsService.getAdminGiftById(id);
            if (old.getImage() != null) {
                ossService.deleteFile(old.getImage());
            }
            dto.setImage(ossService.uploadProductImage(file));
        }
        pointsService.updateAdminGift(id, dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除积分商品")
    @OperateLog(module = "points_gift", type = "delete", description = "删除积分商品「#id」")
    public Result<Void> deleteGift(@PathVariable Long id) {
        pointsService.deleteAdminGift(id);
        return Result.success("删除成功");
    }

    @GetMapping("/orders")
    @Operation(summary = "兑换记录列表")
    public Result<List<ExchangeOrderVO>> getExchangeOrders() {
        return Result.success(pointsService.getAdminExchangeOrders());
    }

    @PutMapping("/orders/{id}/ship")
    @Operation(summary = "发货", description = "积分兑换订单发货")
    @OperateLog(module = "points_gift", type = "ship", description = "积分兑换订单发货「#id」")
    public Result<Void> shipExchangeOrder(@PathVariable Long id,
                                          @RequestBody @Valid ShipExchangeOrderDTO dto) {
        pointsService.adminShipExchangeOrder(id, dto.getLogisticsCompany(), dto.getLogisticsNo());
        return Result.success("发货成功");
    }
}
