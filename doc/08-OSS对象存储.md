# 鸡翅Club (ColaCode) 学习笔记 - OSS 对象存储服务

## 第七章：OSS 对象存储服务

### 7.1 模块概览

OSS 对象存储服务为 ColaCode 提供文件上传、下载和删除功能。当前使用本地存储，后续可通过策略模式无缝切换到 MinIO 或阿里云 OSS。

**核心功能：**
- 📤 文件上传 (支持最大 50MB)
- 📥 文件下载
- 🗑️ 文件删除
- 🔌 策略模式存储适配器 (Local/MinIO/AliyunOSS)

### 7.2 策略模式 - 存储适配器

#### 7.2.1 架构设计

```
StorageAdapter (策略接口)
    ├── adapterType()  → 返回存储类型
    ├── upload()       → 上传文件
    ├── download()     → 下载文件
    ├── delete()       → 删除文件
    └── getAccessUrl() → 获取访问 URL

具体实现:
    └── LocalStorageAdapter (local)  ← 本地文件系统存储
```

#### 7.2.2 策略接口

```java
public interface StorageAdapter {
    String adapterType();
    String upload(MultipartFile file);
    InputStream download(String fileName);
    void delete(String fileName);
    String getAccessUrl(String fileName);
}
```

#### 7.2.3 本地存储实现

```java
@Component
public class LocalStorageAdapter implements StorageAdapter {

    @Value("${storage.local.path:./uploads/}")
    private String storagePath;

    @Value("${storage.local.access-url:http://localhost:4000/oss/download/}")
    private String accessUrl;

    @Override
    public String upload(MultipartFile file) {
        // 1. 创建目录
        File dir = new File(storagePath);
        if (!dir.exists()) dir.mkdirs();

        // 2. 生成唯一文件名 (UUID + 扩展名)
        String fileName = UUID.randomUUID() + extension;

        // 3. 保存文件
        file.transferTo(new File(storagePath + fileName));

        return fileName;
    }
}
```

#### 7.2.4 扩展 MinIO 适配器 (预留)

```java
@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
public class MinioStorageAdapter implements StorageAdapter {

    @Resource
    private MinioClient minioClient;

    @Override
    public String adapterType() {
        return "minio";
    }

    @Override
    public String upload(MultipartFile file) {
        String fileName = UUID.randomUUID() + extension;
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket("colacode")
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build()
        );
        return fileName;
    }
}
```

### 7.3 OSS 服务 API 清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /oss/upload | 上传文件 |
| GET | /oss/download/{fileName} | 下载文件 |
| DELETE | /oss/delete/{fileName} | 删除文件 |

### 7.4 上传响应格式

```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "fileName": "a1b2c3d4e5f6.png",
    "url": "http://localhost:4000/oss/download/a1b2c3d4e5f6.png"
  }
}
```

### 7.5 关键设计

#### 7.5.1 文件名生成

使用 UUID 避免文件名冲突：
```java
String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
```

#### 7.5.2 文件大小限制

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

#### 7.5.3 存储路径配置

```yaml
storage:
  local:
    path: ./uploads/              # 本地存储路径
    access-url: http://localhost:4000/oss/download/  # 访问 URL 前缀
```

---

## 学习总结

### OSS 服务学到的核心知识点
1. ✅ 策略模式在存储适配器中的应用
2. ✅ 本地文件系统存储实现
3. ✅ Spring Boot 文件上传/下载
4. ✅ MultipartFile 处理
5. ✅ UUID 文件名生成
6. ✅ MinIO 适配器预留设计
