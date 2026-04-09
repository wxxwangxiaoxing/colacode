package com.colacode.oss.adapter;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageAdapter {

    String adapterType();

    String upload(MultipartFile file);

    InputStream download(String fileName);

    void delete(String fileName);

    String getAccessUrl(String fileName);
}
