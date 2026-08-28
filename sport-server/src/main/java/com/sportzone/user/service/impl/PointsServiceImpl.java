package com.sportzone.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportzone.admin.dto.CreatePointsGiftDTO;
import com.sportzone.admin.dto.UpdatePointsGiftDTO;
import com.sportzone.user.dto.ExchangeDTO;
import com.sportzone.user.entity.*;
import com.sportzone.user.mapper.*;
import com.sportzone.user.service.PointsService;
import com.sportzone.user.vo.ExchangeOrderVO;
import com.sportzone.user.vo.PointsExchangeOrderVO;
import com.sportzone.user.vo.PointsGiftVO;
import com.sportzone.user.vo.PointsRecordVO;
import com.sportzone.user.vo.SigninStatusVO;
import com.sportzone.user.vo.SigninVO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PointsServiceImpl implements PointsService {

    private final PointsRecordMapper pointsRecordMapper;
    private final PointsGiftMapper pointsGiftMapper;
    private final PointsExchangeOrderMapper exchangeOrderMapper;
    private final UserMapper userMapper;
    private final UserAddressMapper userAddressMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public PointsServiceImpl(PointsRecordMapper pointsRecordMapper, PointsGiftMapper pointsGiftMapper,
                             PointsExchangeOrderMapper exchangeOrderMapper, UserMapper userMapper,
                             UserAddressMapper userAddressMapper,
                             StringRedisTemplate stringRedisTemplate) {
        this.pointsRecordMapper = pointsRecordMapper;
        this.pointsGiftMapper = pointsGiftMapper;
        this.exchangeOrderMapper = exchangeOrderMapper;
        this.userMapper = userMapper;
        this.userAddressMapper = userAddressMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Long getPointsBalance(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getPointsBalance();
    }

    @Override
    public List<PointsRecordVO> getPointsRecordList(Long userId) {
        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRecord::getUserId, userId);
        wrapper.orderByDesc(PointsRecord::getCreateTime);
        return pointsRecordMapper.selectList(wrapper).stream()
                .map(this::toRecordVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PointsGiftVO> getGiftList() {
        return pointsGiftMapper.selectList(null).stream()
                .map(this::toGiftVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PointsExchangeOrderVO exchange(Long userId, ExchangeDTO dto) {
        PointsGift gift = pointsGiftMapper.selectById(dto.getGiftId());
        if (gift == null) {
            throw new RuntimeException("积分商品不存在");
        }
        if (gift.getStock() <= 0) {
            throw new RuntimeException("积分商品库存不足");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getPointsBalance() < gift.getPointsPrice()) {
            throw new RuntimeException("积分不足，当前积分：" + user.getPointsBalance() +
                    "，需要积分：" + gift.getPointsPrice());
        }

        UserAddress address = userAddressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("收货地址不存在");
        }

        user.setPointsBalance(user.getPointsBalance() - gift.getPointsPrice());
        userMapper.updateById(user);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setType(2);
        record.setPoints(Long.valueOf(gift.getPointsPrice()));
        record.setSource("exchange");
        record.setDescription("兑换「" + gift.getName() + "」");
        pointsRecordMapper.insert(record);

        gift.setStock(gift.getStock() - 1);
        pointsGiftMapper.updateById(gift);

        PointsExchangeOrder exchangeOrder = new PointsExchangeOrder();
        exchangeOrder.setOrderNo(generateExchangeNo());
        exchangeOrder.setUserId(userId);
        exchangeOrder.setGiftId(gift.getId());
        exchangeOrder.setGiftName(gift.getName());
        exchangeOrder.setGiftImage(gift.getImage());
        exchangeOrder.setPointsPrice(gift.getPointsPrice());
        exchangeOrder.setStatus(0);
        exchangeOrder.setAddressId(address.getId());
        exchangeOrder.setReceiverName(address.getReceiverName());
        exchangeOrder.setPhone(address.getPhone());
        exchangeOrder.setProvince(address.getProvince());
        exchangeOrder.setCity(address.getCity());
        exchangeOrder.setDistrict(address.getDistrict());
        exchangeOrder.setAddress(address.getAddress());
        exchangeOrder.setRemark(dto.getRemark());
        exchangeOrderMapper.insert(exchangeOrder);

        return toExchangeOrderVO(exchangeOrder);
    }

    @Override
    public List<PointsExchangeOrderVO> getExchangeOrderList(Long userId) {
        LambdaQueryWrapper<PointsExchangeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsExchangeOrder::getUserId, userId);
        wrapper.orderByDesc(PointsExchangeOrder::getCreateTime);
        return exchangeOrderMapper.selectList(wrapper).stream()
                .map(this::toExchangeOrderVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void confirmExchangeOrder(Long userId, Long orderId) {
        PointsExchangeOrder order = exchangeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("兑换订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("当前订单状态不允许确认收货");
        }
        order.setStatus(2);
        order.setCompleteTime(LocalDateTime.now());
        exchangeOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void adminShipExchangeOrder(Long orderId, String logisticsCompany, String logisticsNo) {
        PointsExchangeOrder order = exchangeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("兑换订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态不允许发货");
        }
        order.setStatus(1);
        order.setLogisticsCompany(logisticsCompany);
        order.setLogisticsNo(logisticsNo);
        order.setShipTime(LocalDateTime.now());
        exchangeOrderMapper.updateById(order);
    }

    @Override
    public List<PointsGiftVO> getAdminGiftList() {
        LambdaQueryWrapper<PointsGift> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PointsGift::getCreateTime);
        return pointsGiftMapper.selectList(wrapper).stream()
                .map(this::toGiftVO)
                .collect(Collectors.toList());
    }

    @Override
    public PointsGiftVO getAdminGiftById(Long id) {
        PointsGift gift = pointsGiftMapper.selectById(id);
        if (gift == null) {
            throw new RuntimeException("积分商品不存在");
        }
        return toGiftVO(gift);
    }

    @Override
    public void createAdminGift(CreatePointsGiftDTO dto) {
        PointsGift gift = new PointsGift();
        BeanUtils.copyProperties(dto, gift);
        pointsGiftMapper.insert(gift);
    }

    @Override
    public void updateAdminGift(Long id, UpdatePointsGiftDTO dto) {
        PointsGift gift = pointsGiftMapper.selectById(id);
        if (gift == null) {
            throw new RuntimeException("积分商品不存在");
        }
        if (StringUtils.hasText(dto.getName())) gift.setName(dto.getName());
        if (dto.getImage() != null) gift.setImage(dto.getImage());
        if (dto.getPointsPrice() != null) gift.setPointsPrice(dto.getPointsPrice());
        if (dto.getStock() != null) gift.setStock(dto.getStock());
        if (dto.getDescription() != null) gift.setDescription(dto.getDescription());
        pointsGiftMapper.updateById(gift);
    }

    @Override
    public void deleteAdminGift(Long id) {
        if (pointsGiftMapper.selectById(id) == null) {
            throw new RuntimeException("积分商品不存在");
        }
        pointsGiftMapper.deleteById(id);
    }

    @Override
    public List<ExchangeOrderVO> getAdminExchangeOrders() {
        LambdaQueryWrapper<PointsExchangeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PointsExchangeOrder::getCreateTime);
        return exchangeOrderMapper.selectList(wrapper).stream()
                .map(this::toAdminExchangeOrderVO)
                .collect(Collectors.toList());
    }

    @Override
    public SigninStatusVO signinStatus(Long userId) {
        String todayKey = "signin:" + userId + ":" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        boolean signedIn = Boolean.TRUE.equals(stringRedisTemplate.hasKey(todayKey));
        int streakDays = calculateStreakDays(userId, signedIn);
        int todayBonus = new Random().nextInt(31) + 20;

        SigninStatusVO vo = new SigninStatusVO();
        vo.setSignedIn(signedIn);
        vo.setStreakDays(streakDays);
        vo.setTodayBonus(todayBonus);
        return vo;
    }

    @Override
    @Transactional
    public SigninVO signin(Long userId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String todayKey = "signin:" + userId + ":" + dateStr;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(todayKey))) {
            throw new RuntimeException("今日已签到，请明天再来");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        int points = new Random().nextInt(31) + 20;

        boolean hasToday = false;
        String checkKey = "signin:check:" + userId;
        String cached = stringRedisTemplate.opsForValue().get(checkKey);
        if (cached != null) {
            hasToday = cached.contains(dateStr);
        }
        int streakDays = calculateStreakDays(userId, hasToday) + 1;

        int bonusPoints = 0;
        if (streakDays > 0 && streakDays % 7 == 0) {
            bonusPoints = 100;
        }

        long totalEarned = points + bonusPoints;

        user.setPointsBalance(user.getPointsBalance() + totalEarned);
        userMapper.updateById(user);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setType(1);
        record.setPoints(totalEarned);
        record.setSource("signin");
        String desc = "每日签到获得" + points + "积分";
        if (bonusPoints > 0) {
            desc += "（连续" + streakDays + "天额外奖励" + bonusPoints + "积分）";
        }
        record.setDescription(desc);
        pointsRecordMapper.insert(record);

        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();
        long seconds = Duration.between(LocalDateTime.now(), endOfDay).getSeconds();
        stringRedisTemplate.opsForValue().set(todayKey, String.valueOf(points), seconds, TimeUnit.SECONDS);

        String newVal = (cached != null ? cached + "," : "") + dateStr;
        stringRedisTemplate.opsForValue().set(checkKey, newVal, 72, TimeUnit.HOURS);

        SigninVO vo = new SigninVO();
        vo.setPointsEarned(points);
        vo.setStreakDays(streakDays);
        vo.setBonusPoints(bonusPoints);
        vo.setTotalBalance(user.getPointsBalance());
        return vo;
    }

    private int calculateStreakDays(Long userId, boolean hasToday) {
        Set<LocalDate> signedDates = new HashSet<>();
        List<PointsRecord> records = pointsRecordMapper.selectList(
                new LambdaQueryWrapper<PointsRecord>()
                        .eq(PointsRecord::getUserId, userId)
                        .eq(PointsRecord::getSource, "signin")
                        .ge(PointsRecord::getCreateTime, LocalDate.now().minusDays(31).atStartOfDay()));
        for (PointsRecord r : records) {
            signedDates.add(r.getCreateTime().toLocalDate());
        }

        LocalDate today = LocalDate.now();
        if (!hasToday && !signedDates.contains(today.minusDays(1))) {
            return 0;
        }

        LocalDate start = hasToday || signedDates.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        for (int i = 0; ; i++) {
            if (signedDates.contains(start.minusDays(i))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private PointsRecordVO toRecordVO(PointsRecord record) {
        PointsRecordVO vo = new PointsRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    private PointsGiftVO toGiftVO(PointsGift gift) {
        PointsGiftVO vo = new PointsGiftVO();
        BeanUtils.copyProperties(gift, vo);
        return vo;
    }

    private PointsExchangeOrderVO toExchangeOrderVO(PointsExchangeOrder order) {
        PointsExchangeOrderVO vo = new PointsExchangeOrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    private ExchangeOrderVO toAdminExchangeOrderVO(PointsExchangeOrder order) {
        ExchangeOrderVO vo = new ExchangeOrderVO();
        BeanUtils.copyProperties(order, vo);
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
        }
        return vo;
    }

    private String generateExchangeNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "EX" + date + uuid;
    }
}