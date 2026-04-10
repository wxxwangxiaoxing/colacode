package com.colacode.oss.controller;

import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.oss.adapter.StorageAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/oss")
@Tag(name = "对象存储", description = "文件上传下载管理")
public class OssController {

    private final List<StorageAdapter> adapterList;

    private StorageAdapter storageAdapter;

    public OssController(List<StorageAdapter> adapterList) {
        this.adapterList = adapterList;
        for (StorageAdapter adapter : adapterList) {
            if ("local".equals(adapter.adapterType())) {
                storageAdapter = adapter;
                break;
            }
        }
        if (storageAdapter == null && !adapterList.isEmpty()) {
            storageAdapter = adapterList.get(0);
        }
    }

    @GetMapping("/adapters")
    @Operation(summary = "获取存储适配器", description = "获取支持的存储类型列表")
    public Result<List<String>> listAdapters() {
        List<String> types = adapterList.stream()
                .map(StorageAdapter::adapterType)
                .collect(java.util.stream.Collectors.toList());
        return Result.success(types);
    }

    @PostMapping("/switch")
    @Operation(summary = "切换存储类型", description = "切换文件存储的适配器类型")
    public Result<Void> switchAdapter(@Parameter(description = "存储类型") @RequestParam String type) {
        for (StorageAdapter adapter : adapterList) {
            if (type.equals(adapter.adapterType())) {
                storageAdapter = adapter;
                return Result.success();
            }
        }
        throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "不支持的存储类型: " + type);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到存储服务")
    public Result<Map<String, String>> upload(@Parameter(description = "文件") @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "文件不能为空");
        }

        String fileName = storageAdapter.upload(file);
        String url = storageAdapter.getAccessUrl(fileName);

        Map<String, String> result = new HashMap<>();
        result.put("fileName", fileName);
        result.put("url", url);
        return Result.success(result);
    }

    @GetMapping("/download/{fileName}")
    @Operation(summary = "下载文件", description = "从存储服务下载文件")
    public ResponseEntity<InputStreamResource> download(@Parameter(description = "文件名") @PathVariable String fileName) {
        InputStream inputStream = storageAdapter.download(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping("/delete/{fileName}")
    @Operation(summary = "删除文件", description = "从存储服务删除文件")
    public Result<Void> delete(@Parameter(description = "文件名") @PathVariable String fileName) {
        storageAdapter.delete(fileName);
        return Result.success();
    }
}
