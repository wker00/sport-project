package com.sportzone.user.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.sportzone.user.service.OssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OssServiceImpl implements OssService {

    private static final Logger log = LoggerFactory.getLogger(OssServiceImpl.class);

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.domain}")
    private String domain;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String dir = userId != null ? "avatar/" + userId : "product";
        String fileName = dir + "/" + UUID.randomUUID().toString() + extension;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(bucketName, fileName, inputStream);
        } catch (Exception e) {
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }

        return domain + "/" + fileName;
    }

    @Override
    public String uploadProductImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        String extWithoutDot = extension.startsWith(".") ? extension.substring(1) : extension;
        if (!ALLOWED_EXTENSIONS.contains(extWithoutDot)) {
            throw new RuntimeException("不支持的文件格式，仅支持 jpg/png/gif/webp");
        }
        String fileName = "product/" + UUID.randomUUID() + extension;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(bucketName, fileName, inputStream);
        } catch (Exception e) {
            throw new RuntimeException("商品图片上传失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }

        return domain + "/" + fileName;
    }

    @Override
    public String uploadCategoryImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        String extWithoutDot = extension.startsWith(".") ? extension.substring(1) : extension;
        if (!ALLOWED_EXTENSIONS.contains(extWithoutDot)) {
            throw new RuntimeException("不支持的文件格式，仅支持 jpg/png/gif/webp");
        }
        String fileName = "category/" + UUID.randomUUID() + extension;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(bucketName, fileName, inputStream);
        } catch (Exception e) {
            throw new RuntimeException("分类图片上传失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }

        return domain + "/" + fileName;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(domain)) {
            return;
        }
        String key = fileUrl.substring(domain.length() + 1);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.deleteObject(bucketName, key);
            log.info("已删除OSS文件: {}", key);
        } finally {
            ossClient.shutdown();
        }
    }
}