package com.sportzone.admin.controller;

import com.sportzone.admin.dto.CreateCategoryDTO;
import com.sportzone.admin.dto.UpdateCategoryDTO;
import com.sportzone.common.annotation.OperateLog;
import com.sportzone.user.service.CategoryService;
import com.sportzone.user.service.OssService;
import com.sportzone.user.vo.CategoryVO;
import com.sportzone.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/category")
@Tag(name = "分类管理", description = "管理员分类管理")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final OssService ossService;

    public AdminCategoryController(CategoryService categoryService, OssService ossService) {
        this.categoryService = categoryService;
        this.ossService = ossService;
    }

    @GetMapping("/list")
    @Operation(summary = "获取分类列表")
    public Result<List<CategoryVO>> getCategoryList() {
        return Result.success(categoryService.getCategoryList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "分类详情")
    public Result<CategoryVO> getCategoryById(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建分类（带图片上传）")
    @OperateLog(module = "category", type = "create", description = "创建分类「#dto.name」")
    public Result<Void> createCategory(
            @RequestPart("category") @Valid CreateCategoryDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            dto.setImage(ossService.uploadCategoryImage(file));
        }
        categoryService.createCategory(dto);
        return Result.success("创建成功");
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "更新分类（带图片上传）")
    @OperateLog(module = "category", type = "update", description = "更新分类「#id」")
    public Result<Void> updateCategory(
            @PathVariable Long id,
            @RequestPart("category") @Valid UpdateCategoryDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            CategoryVO old = categoryService.getCategoryById(id);
            if (old.getImage() != null) {
                ossService.deleteFile(old.getImage());
            }
            dto.setImage(ossService.uploadCategoryImage(file));
        }
        categoryService.updateCategory(id, dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    @OperateLog(module = "category", type = "delete", description = "删除分类「#id」")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功");
    }
}
