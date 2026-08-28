package com.sportzone.admin.controller;

import com.sportzone.admin.mapper.OperateLogMapper;
import com.sportzone.admin.service.OperateLogService;
import com.sportzone.admin.vo.OperateLogVO;
import com.sportzone.common.dto.PageResult;
import com.sportzone.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/operate-log")
@Tag(name = "操作日志管理", description = "管理员操作日志查询与清理")
public class AdminOperateLogController {

    private final OperateLogService operateLogService;
    private final OperateLogMapper operateLogMapper;

    public AdminOperateLogController(OperateLogService operateLogService, OperateLogMapper operateLogMapper) {
        this.operateLogService = operateLogService;
        this.operateLogMapper = operateLogMapper;
    }

    @GetMapping("/list")
    @Operation(summary = "操作日志列表（分页）")
    public Result<PageResult<OperateLogVO>> listOperateLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(operateLogService.listOperateLogs(module, type, startDate, endDate, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "操作日志详情")
    public Result<OperateLogVO> getOperateLogById(@PathVariable Long id) {
        return Result.success(operateLogService.getOperateLogById(id));
    }

    @DeleteMapping("/clean")
    @Operation(summary = "清理操作日志", description = "删除指定日期前的操作日志")
    public Result<Void> cleanOperateLog(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beforeDate) {
        operateLogService.cleanOperateLog(beforeDate);
        return Result.success("清理成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除操作日志")
    public Result<Void> deleteOperateLog(@PathVariable Long id) {
        operateLogService.getOperateLogById(id);
        operateLogMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
