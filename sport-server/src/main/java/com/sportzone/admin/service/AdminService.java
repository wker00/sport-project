package com.sportzone.admin.service;

import com.sportzone.admin.dto.AdminLoginDTO;
import com.sportzone.admin.dto.AdminRegisterDTO;
import com.sportzone.admin.dto.AdminUpdateDTO;
import com.sportzone.admin.dto.AdminChangePasswordDTO;
import com.sportzone.admin.dto.ReviewReplyDTO;
import com.sportzone.admin.vo.AdminVO;
import com.sportzone.admin.vo.DashboardVO;
import com.sportzone.admin.dto.OrderShipDTO;
import com.sportzone.admin.dto.OrderRefundDTO;

import java.util.List;

public interface AdminService {

    String login(AdminLoginDTO dto, String ip);

    void logout(Long adminId);

    void register(Long currentAdminId, AdminRegisterDTO dto);

    AdminVO getAdminInfo(Long adminId);

    void updateAdminInfo(Long adminId, AdminUpdateDTO dto);

    void updateAdminInfoById(Long currentAdminId, Long targetId, AdminUpdateDTO dto);

    void updateAvatar(Long adminId, String avatarUrl);

    void changePassword(Long adminId, AdminChangePasswordDTO dto);

    List<AdminVO> listAdmins();

    void deleteAdmin(Long currentAdminId, Long targetAdminId);

    void enableAdmin(Long currentAdminId, Long id);

    void disableAdmin(Long currentAdminId, Long targetAdminId);

    void resetAdminPassword(Long currentAdminId, Long targetAdminId);

    DashboardVO getDashboard();

    void shipOrder(Long adminId, OrderShipDTO dto);

    void deliverOrder(Long adminId, Long orderId);

    void processRefund(Long adminId, OrderRefundDTO dto);

    void replyReview(Long adminId, Long reviewId, ReviewReplyDTO dto);

    void deleteReview(Long adminId, Long reviewId);

}
