package com.library.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeBookPdf(MultipartFile file);
    String storeCoverImage(MultipartFile file);
    String storeProfileImage(MultipartFile file);
    void deleteFile(String filePath);
}