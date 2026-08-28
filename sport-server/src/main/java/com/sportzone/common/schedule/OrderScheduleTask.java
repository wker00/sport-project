package com.sportzone.common.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportzone.user.entity.Order;
import com.sportzone.user.mapper.OrderMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduleTask {

    private final OrderMapper orderMapper;

    public OrderScheduleTask(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Scheduled(fixedRate = 600000)
    @Transactional
    public void autoDeliverOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 2)
                .isNotNull(Order::getDeliveryTime)
                .le(Order::getDeliveryTime, threshold);
        List<Order> orders = orderMapper.selectList(wrapper);
        for (Order order : orders) {
            order.setStatus(3);
            orderMapper.updateById(order);
        }
    }
}
