package com.library.service.impl;

import com.library.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.books-dir:uploads/books}")
    private String booksDir;

    @Value("${app.upload.covers-dir:uploads/covers}")
    private String coversDir;

    private static final String PROFILES_DIR = "uploads/profiles";

    @Override
    public String storeBookPdf(MultipartFile file) {
        return storeFile(file, booksDir, "pdf");
    }

    @Override
    public String storeCoverImage(MultipartFile file) {
        return storeFile(file, coversDir, "image");
    }

    @Override
    public String storeProfileImage(MultipartFile file) {
        return storeFile(file, PROFILES_DIR, "image");
    }

    private String storeFile(MultipartFile file, String dir, String type) {
        if (file == null || file.isEmpty()) return null;

        try {
            Path dirPath = Paths.get(dir).toAbsolutePath();
            Files.createDirectories(dirPath);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Validate extension
            if ("pdf".equals(type) && !extension.equalsIgnoreCase(".pdf")) {
                throw new RuntimeException("Only PDF files are allowed");
            }
            if ("image".equals(type) && !isImageExtension(extension)) {
                throw new RuntimeException("Only image files are allowed (jpg, png, gif, webp)");
            }

            String filename = UUID.randomUUID().toString() + extension;
            Path targetPath = dirPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return dir + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file: " + e.getMessage(), e);
        }
    }

    private boolean isImageExtension(String extension) {
        return extension.equalsIgnoreCase(".jpg") ||
               extension.equalsIgnoreCase(".jpeg") ||
               extension.equalsIgnoreCase(".png") ||
               extension.equalsIgnoreCase(".gif") ||
               extension.equalsIgnoreCase(".webp");
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null) return;
        try {
            Path path = Paths.get(filePath).toAbsolutePath();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // log but don't throw
        }
    }
}