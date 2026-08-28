package com.sportzone.user.service;

import com.sportzone.admin.dto.AdminCreateCouponDTO;
import com.sportzone.admin.dto.AdminUpdateCouponDTO;
import com.sportzone.user.dto.ClaimCouponDTO;
import com.sportzone.user.vo.CouponRecordVO;
import com.sportzone.user.vo.CouponVO;
import com.sportzone.user.vo.UserCouponVO;

import java.util.List;

public interface CouponService {

    List<CouponVO> getAvailableCouponList();

    List<UserCouponVO> getMyCoupons(Long userId);

    UserCouponVO claimCoupon(Long userId, ClaimCouponDTO dto);

    List<CouponVO> getAdminCouponList();

    CouponVO getAdminCouponById(Long id);

    void createAdminCoupon(AdminCreateCouponDTO dto);

    void updateAdminCoupon(Long id, AdminUpdateCouponDTO dto);

    void deleteAdminCoupon(Long id);

    List<CouponRecordVO> getCouponRecords(Long couponId);
}
