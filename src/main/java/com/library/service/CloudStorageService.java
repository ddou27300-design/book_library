package com.library.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudStorageService {
    String uploadFile(MultipartFile file, String folder);
    void deleteFile(String fileUrl);
}
