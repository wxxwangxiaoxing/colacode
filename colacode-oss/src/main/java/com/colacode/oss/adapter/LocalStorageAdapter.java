package com.colacode.oss.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
public class LocalStorageAdapter implements StorageAdapter {

    @Value("${storage.local.path:./uploads/}")
    private String storagePath;

    @Value("${storage.local.access-url:http://localhost:4000/oss/download/}")
    private String accessUrl;

    @Override
    public String adapterType() {
        return "local";
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            File dir = new File(storagePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

            File dest = new File(storagePath + fileName);
            file.transferTo(dest);

            log.info("本地文件上传成功: {}", fileName);
            return fileName;
        } catch (IOException e) {
            log.error("本地文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream download(String fileName) {
        try {
            File file = new File(storagePath + fileName);
            if (!file.exists()) {
                throw new RuntimeException("文件不存在: " + fileName);
            }
            return new FileInputStream(file);
        } catch (IOException e) {
            log.error("文件下载失败: {}", fileName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String fileName) {
        File file = new File(storagePath + fileName);
        if (file.exists()) {
            file.delete();
            log.info("本地文件删除成功: {}", fileName);
        }
    }

    @Override
    public String getAccessUrl(String fileName) {
        return accessUrl + fileName;
    }
}
