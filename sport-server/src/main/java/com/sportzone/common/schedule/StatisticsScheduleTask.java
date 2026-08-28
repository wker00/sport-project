package com.sportzone.common.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportzone.admin.entity.StatisticsDaily;
import com.sportzone.admin.mapper.StatisticsDailyMapper;
import com.sportzone.user.entity.Order;
import com.sportzone.user.entity.User;
import com.sportzone.user.mapper.OrderMapper;
import com.sportzone.user.mapper.UserMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StatisticsScheduleTask {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final StatisticsDailyMapper statisticsDailyMapper;

    public StatisticsScheduleTask(UserMapper userMapper, OrderMapper orderMapper,
                                  StatisticsDailyMapper statisticsDailyMapper) {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.statisticsDailyMapper = statisticsDailyMapper;
    }

    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void collectDailyStatistics() {
        LocalDate statDate = LocalDate.now().minusDays(1);
        LocalDateTime start = statDate.atStartOfDay();
        LocalDateTime end = statDate.plusDays(1).atStartOfDay();

        long newUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .ge(User::getCreateTime, start)
                        .lt(User::getCreateTime, end));

        List<Order> allOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .ge(Order::getCreateTime, start)
                        .lt(Order::getCreateTime, end));

        long activeUsers = allOrders.stream()
                .map(Order::getUserId)
                .distinct()
                .count();

        List<Order> validOrders = allOrders.stream()
                .filter(o -> o.getStatus() != 6)
                .collect(Collectors.toList());
        int newOrders = validOrders.size();
        BigDecimal orderAmount = validOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Order> paidOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .in(Order::getStatus, 1, 2, 3, 4, 5)
                        .ge(Order::getUpdateTime, start)
                        .lt(Order::getUpdateTime, end));
        BigDecimal payAmount = paidOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refundAmount = paidOrders.stream()
                .filter(o -> o.getRefundStatus() != null && o.getRefundStatus() == 2)
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StatisticsDaily stats = statisticsDailyMapper.selectOne(
                new LambdaQueryWrapper<StatisticsDaily>()
                        .eq(StatisticsDaily::getStatDate, statDate));
        if (stats == null) {
            stats = new StatisticsDaily();
            stats.setStatDate(statDate);
            stats.setNewUsers((int) newUsers);
            stats.setActiveUsers((int) activeUsers);
            stats.setNewOrders(newOrders);
            stats.setOrderAmount(orderAmount);
            stats.setPayAmount(payAmount);
            stats.setRefundAmount(refundAmount);
            statisticsDailyMapper.insert(stats);
        } else {
            stats.setNewUsers((int) newUsers);
            stats.setActiveUsers((int) activeUsers);
            stats.setNewOrders(newOrders);
            stats.setOrderAmount(orderAmount);
            stats.setPayAmount(payAmount);
            stats.setRefundAmount(refundAmount);
            statisticsDailyMapper.updateById(stats);
        }
    }
}
