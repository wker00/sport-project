package com.sportzone.user.service;

import com.sportzone.user.dto.ChangePasswordDTO;
import com.sportzone.user.dto.LoginDTO;
import com.sportzone.user.dto.RegisterDTO;
import com.sportzone.user.dto.UpdateUserDTO;
import com.sportzone.user.vo.UserVO;

public interface UserService {

    void register(RegisterDTO dto);

    String login(LoginDTO dto);

    UserVO getUserInfo(Long userId);

    void updateUserInfo(Long userId, UpdateUserDTO dto);

    void updateAvatar(Long userId, String avatarUrl);

    void changePassword(Long userId, ChangePasswordDTO dto);

    void upgradeLevel(Long userId);

    void logout(Long userId);
}