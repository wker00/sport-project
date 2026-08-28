package com.sportzone.user.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {

    String uploadAvatar(MultipartFile file, Long userId);

    String uploadProductImage(MultipartFile file);

    String uploadCategoryImage(MultipartFile file);

    void deleteFile(String fileUrl);
}