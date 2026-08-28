package com.sportzone.admin.controller;

import com.sportzone.common.annotation.OperateLog;
import com.sportzone.common.dto.PageResult;
import com.sportzone.user.dto.CreateProductDTO;
import com.sportzone.user.dto.UpdateProductDTO;
import com.sportzone.user.service.OssService;
import com.sportzone.user.service.ProductService;
import com.sportzone.user.vo.ProductVO;
import com.sportzone.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/product")
@Tag(name = "商品管理", description = "管理员商品管理")
public class AdminProductController {

    private final ProductService productService;
    private final OssService ossService;

    public AdminProductController(ProductService productService, OssService ossService) {
        this.productService = productService;
        this.ossService = ossService;
    }

    @PostMapping("/upload")
    @Operation(summary = "上传商品图片")
    @OperateLog(module = "product", type = "upload", description = "上传商品图片")
    public Result<String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        String url = ossService.uploadProductImage(file);
        return Result.success("上传成功", url);
    }

    @PostMapping
    @Operation(summary = "创建商品")
    @OperateLog(module = "product", type = "create", description = "创建商品「#dto.name」")
    public Result<Void> createProduct(@RequestBody @Valid CreateProductDTO dto) {
        productService.createProduct(dto);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品")
    @OperateLog(module = "product", type = "update", description = "更新商品「#id」")
    public Result<Void> updateProduct(@PathVariable Long id,
                                      @RequestBody @Valid UpdateProductDTO dto) {
        ProductVO old = productService.getProductById(id);
        productService.updateProduct(id, dto, old.getImage());
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    @OperateLog(module = "product", type = "delete", description = "删除商品「#id」")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        ProductVO product = productService.getProductById(id);
        if (product.getImage() != null) {
            ossService.deleteFile(product.getImage());
        }
        productService.deleteProduct(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "商品详情")
    public Result<ProductVO> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "商品列表（分页）")
    public Result<PageResult<ProductVO>> getProductList(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(productService.getProductPage(null, null, "desc", page, size));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "按分类获取商品")
    public Result<List<ProductVO>> getProductsByCategory(@PathVariable Long categoryId) {
        return Result.success(productService.getProductList(categoryId, null, "desc"));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索商品")
    public Result<List<ProductVO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(productService.searchProducts(keyword, categoryId, null, "desc"));
    }

}
