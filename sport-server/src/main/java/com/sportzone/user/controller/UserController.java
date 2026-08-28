package com.sportzone.user.controller;

import com.sportzone.user.dto.ChangePasswordDTO;
import com.sportzone.user.dto.LoginDTO;
import com.sportzone.user.dto.RegisterDTO;
import com.sportzone.user.dto.UpdateUserDTO;
import com.sportzone.user.service.OssService;
import com.sportzone.user.service.UserService;
import com.sportzone.utils.Result;
import com.sportzone.utils.RsaUtils;
import com.sportzone.utils.ThreadLocalUtil;
import com.sportzone.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户注册、登录、信息管理")
public class UserController {

    private final UserService userService;
    private final OssService ossService;
    private final RsaUtils rsaUtils;

    public UserController(UserService userService, OssService ossService, RsaUtils rsaUtils) {
        this.userService = userService;
        this.ossService = ossService;
        this.rsaUtils = rsaUtils;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        String password = rsaUtils.decrypt(dto.getPassword());
        String confirmPassword = rsaUtils.decrypt(dto.getConfirmPassword());
        if (password.length() < 6 || password.length() > 20) {
            return Result.error(400, "密码长度6-20位");
        }
        if (!password.equals(confirmPassword)) {
            return Result.error(400, "两次输入的密码不一致");
        }
        dto.setPassword(password);
        dto.setConfirmPassword(confirmPassword);
        userService.register(dto);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT令牌")
    public Result<String> login(@RequestBody @Valid LoginDTO dto) {
        dto.setPassword(rsaUtils.decrypt(dto.getPassword()));
        return Result.success("登录成功", userService.login(dto));
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息（含累计消费金额）")
    public Result<UserVO> getUserInfo() {
        return Result.success(userService.getUserInfo(ThreadLocalUtil.getUserId()));
    }

    @PutMapping("/updateInfo")
    @Operation(summary = "更新用户信息", description = "更新昵称、邮箱、手机号")
    public Result<Void> updateUserInfo(@RequestBody @Valid UpdateUserDTO dto) {
        userService.updateUserInfo(ThreadLocalUtil.getUserId(), dto);
        return Result.success("信息更新成功");
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传并更新用户头像")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }
        UserVO oldInfo = userService.getUserInfo(ThreadLocalUtil.getUserId());
        if (oldInfo.getAvatar() != null) {
            ossService.deleteFile(oldInfo.getAvatar());
        }
        String avatarUrl = ossService.uploadAvatar(file, ThreadLocalUtil.getUserId());
        userService.updateAvatar(ThreadLocalUtil.getUserId(), avatarUrl);
        return Result.success(avatarUrl);
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前用户的密码")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
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
        userService.changePassword(ThreadLocalUtil.getUserId(), dto);
        return Result.success("密码修改成功,请重新登录");
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "退出当前登录状态")
    public Result<Void> logout() {
        userService.logout(ThreadLocalUtil.getUserId());
        return Result.success("退出成功");
    }
}