package com.sportzone.admin.controller;

import com.sportzone.admin.dto.AdminLoginDTO;
import com.sportzone.admin.dto.AdminRegisterDTO;
import com.sportzone.admin.dto.AdminUpdateDTO;
import com.sportzone.admin.dto.AdminChangePasswordDTO;
import com.sportzone.admin.service.AdminService;
import com.sportzone.admin.vo.AdminVO;
import com.sportzone.common.annotation.OperateLog;
import com.sportzone.user.service.OssService;
import com.sportzone.utils.Result;
import com.sportzone.utils.RsaUtils;
import com.sportzone.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员认证", description = "管理员登录、退出、信息管理")
public class AdminAuthController {

    private final AdminService adminService;
    private final OssService ossService;
    private final RsaUtils rsaUtils;
    private final HttpServletRequest request;

    public AdminAuthController(AdminService adminService, OssService ossService, RsaUtils rsaUtils, HttpServletRequest request) {
        this.adminService = adminService;
        this.ossService = ossService;
        this.rsaUtils = rsaUtils;
        this.request = request;
    }

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    @OperateLog(module = "admin", type = "login", description = "管理员登录")
    public Result<String> login(@RequestBody @Valid AdminLoginDTO dto) {
        dto.setPassword(rsaUtils.decrypt(dto.getPassword()));
        String ip = getClientIp(request);
        return Result.success("登录成功", adminService.login(dto, ip));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    @OperateLog(module = "admin", type = "logout", description = "退出登录")
    public Result<Void> logout() {
        adminService.logout(ThreadLocalUtil.getUserId());
        return Result.success("退出成功");
    }

    @PostMapping("/register")
    @Operation(summary = "创建管理员（仅超级管理员可用）")
    @OperateLog(module = "admin", type = "create", description = "创建管理员「#dto.username」")
    public Result<Void> register(@RequestBody @Valid AdminRegisterDTO dto) {
        dto.setPassword(rsaUtils.decrypt(dto.getPassword()));
        adminService.register(ThreadLocalUtil.getUserId(), dto);
        return Result.success("创建成功");
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前管理员信息")
    public Result<AdminVO> getAdminInfo() {
        return Result.success(adminService.getAdminInfo(ThreadLocalUtil.getUserId()));
    }

    @PutMapping("/info")
    @Operation(summary = "更新管理员信息")
    @OperateLog(module = "admin", type = "update", description = "更新个人信息")
    public Result<Void> updateAdminInfo(@RequestBody @Valid AdminUpdateDTO dto) {
        adminService.updateAdminInfo(ThreadLocalUtil.getUserId(), dto);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/info")
    @Operation(summary = "超级管理员更新其他管理员信息")
    @OperateLog(module = "admin", type = "update", description = "更新管理员「#id」信息")
    public Result<Void> updateAdminInfoById(@PathVariable Long id, @RequestBody AdminUpdateDTO dto) {
        adminService.updateAdminInfoById(ThreadLocalUtil.getUserId(), id, dto);
        return Result.success("更新成功");
    }

    @GetMapping("/list")
    @Operation(summary = "获取管理员列表")
    public Result<java.util.List<AdminVO>> listAdmins() {
        return Result.success(adminService.listAdmins());
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用管理员（仅超级管理员可用）")
    @OperateLog(module = "admin", type = "enable", description = "启用管理员「#id」")
    public Result<Void> enableAdmin(@PathVariable Long id) {
        adminService.enableAdmin(ThreadLocalUtil.getUserId(), id);
        return Result.success("启用成功");
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用管理员")
    @OperateLog(module = "admin", type = "disable", description = "禁用管理员「#id」")
    public Result<Void> disableAdmin(@PathVariable Long id) {
        adminService.disableAdmin(ThreadLocalUtil.getUserId(), id);
        return Result.success("禁用成功");
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "重置管理员密码", description = "重置为默认密码 123456（仅超级管理员可用）")
    @OperateLog(module = "admin", type = "reset_password", description = "重置管理员「#id」密码")
    public Result<Void> resetAdminPassword(@PathVariable Long id) {
        adminService.resetAdminPassword(ThreadLocalUtil.getUserId(), id);
        return Result.success("重置成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除管理员")
    @OperateLog(module = "admin", type = "delete", description = "删除管理员「#id」")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(ThreadLocalUtil.getUserId(), id);
        return Result.success("删除成功");
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    @OperateLog(module = "admin", type = "change_password", description = "修改密码")
    public Result<Void> changePassword(@RequestBody @Valid AdminChangePasswordDTO dto) {
        String oldPassword = rsaUtils.decrypt(dto.getOldPassword());
        String newPassword = rsaUtils.decrypt(dto.getNewPassword());
        String confirmPassword = rsaUtils.decrypt(dto.getConfirmPassword());
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            return Result.error(400, "新密码长度6-20位");
        }
        if (!newPassword.equals(confirmPassword)) {
            return Result.error(400, "两次输入的密码不一致");
        }
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmPassword(confirmPassword);
        adminService.changePassword(ThreadLocalUtil.getUserId(), dto);
        return Result.success("修改成功");
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像")
    @OperateLog(module = "admin", type = "update", description = "上传头像")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }
        Long adminId = ThreadLocalUtil.getUserId();
        AdminVO oldInfo = adminService.getAdminInfo(adminId);
        if (oldInfo.getAvatar() != null) {
            ossService.deleteFile(oldInfo.getAvatar());
        }
        String avatarUrl = ossService.uploadAvatar(file, adminId);
        adminService.updateAvatar(adminId, avatarUrl);
        return Result.success("上传成功",avatarUrl);
    }
}
