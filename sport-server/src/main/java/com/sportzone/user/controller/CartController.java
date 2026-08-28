package com.sportzone.user.controller;

import com.sportzone.user.dto.CartDTO;
import com.sportzone.user.dto.UpdateCartDTO;
import com.sportzone.user.service.CartService;
import com.sportzone.utils.Result;
import com.sportzone.utils.ThreadLocalUtil;
import com.sportzone.user.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/cart")
@Tag(name = "购物车管理", description = "购物车商品管理")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    @Operation(summary = "添加购物车", description = "添加商品到购物车，已存在则增加数量")
    public Result<Long> addCart(@RequestBody @Valid CartDTO dto) {
        Long cartId = cartService.addCart(ThreadLocalUtil.getUserId(), dto);
        return Result.success("添加成功", cartId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新购物车", description = "更新购物车商品数量或规格")
    public Result<Void> updateCart(@PathVariable Long id, @RequestBody @Valid UpdateCartDTO dto) {
        cartService.updateCart(ThreadLocalUtil.getUserId(), id, dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除购物车项", description = "删除指定的购物车商品")
    public Result<Void> deleteCart(@PathVariable Long id) {
        cartService.deleteCart(ThreadLocalUtil.getUserId(), id);
        return Result.success("删除成功");
    }

    @DeleteMapping
    @Operation(summary = "清空购物车", description = "清空当前用户的所有购物车商品")
    public Result<Void> clearCart() {
        cartService.clearCart(ThreadLocalUtil.getUserId());
        return Result.success("已清空");
    }

    @GetMapping
    @Operation(summary = "购物车列表", description = "获取当前用户的购物车商品列表")
    public Result<List<CartVO>> getCartList() {
        return Result.success(cartService.getCartList(ThreadLocalUtil.getUserId()));
    }

    @PutMapping("/{id}/check")
    @Operation(summary = "选中/取消选中", description = "切换购物车商品的选中状态")
    public Result<Void> checkCart(@PathVariable Long id, @RequestParam Integer checked) {
        cartService.checkCart(ThreadLocalUtil.getUserId(), id, checked);
        return Result.success(checked == 1 ? "已选中" : "已取消选中");
    }
}