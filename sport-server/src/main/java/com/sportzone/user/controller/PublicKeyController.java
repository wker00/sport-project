package com.sportzone.user.controller;

import com.sportzone.utils.Result;
import com.sportzone.utils.RsaUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@Tag(name = "公共服务", description = "无需认证的公共接口")
public class PublicKeyController {

    private final RsaUtils rsaUtils;

    public PublicKeyController(RsaUtils rsaUtils) {
        this.rsaUtils = rsaUtils;
    }

    @GetMapping("/key")
    @Operation(summary = "获取RSA公钥", description = "前端用于加密密码传输")
    public Result<String> getPublicKey() {
        return Result.success(rsaUtils.getPublicKeyBase64());
    }
}
