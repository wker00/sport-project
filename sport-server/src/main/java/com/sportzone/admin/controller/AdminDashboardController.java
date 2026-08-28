package com.sportzone.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportzone.admin.entity.StatisticsDaily;
import com.sportzone.admin.mapper.StatisticsDailyMapper;
import com.sportzone.admin.service.AdminService;
import com.sportzone.admin.vo.DashboardVO;
import com.sportzone.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "仪表盘与统计", description = "数据统计概览与每日统计")
public class AdminDashboardController {

    private final AdminService adminService;
    private final StatisticsDailyMapper statisticsDailyMapper;

    public AdminDashboardController(AdminService adminService, StatisticsDailyMapper statisticsDailyMapper) {
        this.adminService = adminService;
        this.statisticsDailyMapper = statisticsDailyMapper;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表盘数据")
    public Result<DashboardVO> getDashboard() {
        return Result.success(adminService.getDashboard());
    }

    @GetMapping("/statistics/daily")
    @Operation(summary = "获取每日统计数据")
    public Result<List<StatisticsDaily>> getDailyStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        LambdaQueryWrapper<StatisticsDaily> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StatisticsDaily::getStatDate);
        if (startDate != null) {
            wrapper.ge(StatisticsDaily::getStatDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(StatisticsDaily::getStatDate, endDate);
        }
        return Result.success(statisticsDailyMapper.selectList(wrapper));
    }
}
