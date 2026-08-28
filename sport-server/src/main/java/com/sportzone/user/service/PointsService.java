package com.sportzone.user.service;

import com.sportzone.admin.dto.CreatePointsGiftDTO;
import com.sportzone.admin.dto.UpdatePointsGiftDTO;
import com.sportzone.user.dto.ExchangeDTO;
import com.sportzone.user.vo.ExchangeOrderVO;
import com.sportzone.user.vo.PointsExchangeOrderVO;
import com.sportzone.user.vo.PointsGiftVO;
import com.sportzone.user.vo.PointsRecordVO;
import com.sportzone.user.vo.SigninStatusVO;
import com.sportzone.user.vo.SigninVO;

import java.util.List;

public interface PointsService {

    Long getPointsBalance(Long userId);

    List<PointsRecordVO> getPointsRecordList(Long userId);

    List<PointsGiftVO> getGiftList();

    PointsExchangeOrderVO exchange(Long userId, ExchangeDTO dto);

    List<PointsExchangeOrderVO> getExchangeOrderList(Long userId);

    List<PointsGiftVO> getAdminGiftList();

    PointsGiftVO getAdminGiftById(Long id);

    void createAdminGift(CreatePointsGiftDTO dto);

    void updateAdminGift(Long id, UpdatePointsGiftDTO dto);

    void deleteAdminGift(Long id);

    void confirmExchangeOrder(Long userId, Long orderId);

    void adminShipExchangeOrder(Long orderId, String logisticsCompany, String logisticsNo);

    List<ExchangeOrderVO> getAdminExchangeOrders();

    SigninStatusVO signinStatus(Long userId);

    SigninVO signin(Long userId);
}