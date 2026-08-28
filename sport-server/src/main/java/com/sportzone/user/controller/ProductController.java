package com.sportzone.user.controller;

import com.sportzone.user.service.CategoryService;
import com.sportzone.user.service.ProductService;
import com.sportzone.user.vo.CategoryVO;
import com.sportzone.user.vo.ProductReviewVO;
import com.sportzone.user.vo.ProductVO;
import com.sportzone.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/product")
@Tag(name = "商品浏览", description = "商品查询、搜索")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情", description = "根据ID获取商品详细信息")
    public Result<ProductVO> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "获取商品评价列表", description = "获取指定商品的所有评价，按评价时间降序")
    public Result<List<ProductReviewVO>> getProductReviews(@PathVariable Long id) {
        return Result.success(productService.getProductReviews(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获取商品列表", description = "获取所有上架商品，支持按分类筛选和排序")
    public Result<List<ProductVO>> getProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        return Result.success(productService.getProductList(categoryId, sortBy, sortDir));
    }

    @GetMapping("/hot")
    @Operation(summary = "热门推荐", description = "获取热门推荐商品，按销量降序返回前5个")
    public Result<List<ProductVO>> getHotProducts() {
        return Result.success(productService.getHotProducts());
    }

    @GetMapping("/categories")
    @Operation(summary = "获取分类列表", description = "limit可选，不传返回全部，按排序升序")
    public Result<List<CategoryVO>> getCategoryList(
            @RequestParam(required = false) Integer limit) {
        return Result.success(categoryService.getTopCategories(limit));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "根据关键词搜索商品，支持排序")
    public Result<List<ProductVO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        return Result.success(productService.searchProducts(keyword, null, sortBy, sortDir));
    }
}