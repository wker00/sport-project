package com.sportzone.user.controller;

import com.sportzone.user.dto.AddressDTO;
import com.sportzone.user.service.AddressService;
import com.sportzone.utils.Result;
import com.sportzone.utils.ThreadLocalUtil;
import com.sportzone.user.vo.AddressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/address")
@Tag(name = "地址管理", description = "用户收货地址管理")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @Operation(summary = "添加地址", description = "添加新的收货地址")
    public Result<Void> addAddress(@RequestBody @Valid AddressDTO dto) {
        addressService.addAddress(ThreadLocalUtil.getUserId(), dto);
        return Result.success("地址添加成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新地址", description = "更新收货地址信息")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody @Valid AddressDTO dto) {
        addressService.updateAddress(ThreadLocalUtil.getUserId(), id, dto);
        return Result.success("地址更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址", description = "删除收货地址")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(ThreadLocalUtil.getUserId(), id);
        return Result.success("地址删除成功");
    }

    @GetMapping
    @Operation(summary = "获取地址列表", description = "获取当前用户的所有收货地址")
    public Result<List<AddressVO>> getAddressList() {
        return Result.success(addressService.getAddressList(ThreadLocalUtil.getUserId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取地址详情", description = "获取指定收货地址的详细信息")
    public Result<AddressVO> getAddressById(@PathVariable Long id) {
        return Result.success(addressService.getAddressById(ThreadLocalUtil.getUserId(), id));
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "设置默认地址", description = "将指定地址设为默认收货地址")
    public Result<Void> setDefaultAddress(@PathVariable Long id) {
        addressService.setDefaultAddress(ThreadLocalUtil.getUserId(), id);
        return Result.success("设置默认地址成功");
    }
}