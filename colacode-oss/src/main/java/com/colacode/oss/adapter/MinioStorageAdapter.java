package com.colacode.oss.adapter;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
public class MinioStorageAdapter implements StorageAdapter {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String endpoint;

    public MinioStorageAdapter(
            @org.springframework.beans.factory.annotation.Value("${storage.minio.endpoint}") String endpoint,
            @org.springframework.beans.factory.annotation.Value("${storage.minio.access-key}") String accessKey,
            @org.springframework.beans.factory.annotation.Value("${storage.minio.secret-key}") String secretKey,
            @org.springframework.beans.factory.annotation.Value("${storage.minio.bucket-name}") String bucketName) {
        this.endpoint = endpoint;
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        initBucket();
    }

    private void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO Bucket [{}] 创建成功", bucketName);
            }
        } catch (Exception e) {
            log.error("MinIO Bucket 初始化失败", e);
        }
    }

    @Override
    public String adapterType() {
        return "minio";
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = UUID.randomUUID().toString().replace("-", "") + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("MinIO文件上传成功: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("MinIO文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream download(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO文件下载失败: {}", fileName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("MinIO文件删除成功: {}", fileName);
        } catch (Exception e) {
            log.error("MinIO文件删除失败: {}", fileName, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public String getAccessUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(7, java.util.concurrent.TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取MinIO文件URL失败: {}", fileName, e);
            return endpoint + "/" + bucketName + "/" + fileName;
        }
    }
}
