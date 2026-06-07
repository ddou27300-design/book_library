package com.library.service.impl;

import com.library.service.CloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class CloudStorageServiceImpl implements CloudStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudStorageServiceImpl.class);

    // ============================================================
    // PASTE YOUR SUPABASE CREDENTIALS HERE:
    // 1. Go to Supabase Dashboard -> Project Settings -> API
    // 2. Copy "Project URL" into app.supabase.url in application.properties
    // 3. Copy "anon / public" key into app.supabase.anon-key in application.properties
    // ============================================================

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.anon-key}")
    private String supabaseAnonKey;

    @Value("${app.supabase.bucket}")
    private String bucketName;

    private final RestTemplate restTemplate;

    public CloudStorageServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            String objectPath = folder + "/" + uniqueFilename;

            // Supabase Storage REST API endpoint:
            // POST https://{project_ref}.supabase.co/storage/v1/object/{bucket}/{path}
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + objectPath;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));
            headers.set("apikey", supabaseAnonKey);
            headers.set("Authorization", "Bearer " + supabaseAnonKey);

            byte[] fileBytes = file.getBytes();
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                // Construct the public URL for the uploaded file
                String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + objectPath;
                log.info("File uploaded successfully to Supabase: {}", publicUrl);
                return publicUrl;
            } else {
                throw new RuntimeException("Supabase upload failed with status: " + response.getStatusCode());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file for upload: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Supabase: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract the object path from the public URL
            // URL format: https://{project}.supabase.co/storage/v1/object/public/{bucket}/{folder}/{filename}
            String marker = "/storage/v1/object/public/" + bucketName + "/";
            int idx = fileUrl.indexOf(marker);
            if (idx == -1) {
                log.warn("Could not parse Supabase URL for deletion: {}", fileUrl);
                return;
            }
            String objectPath = fileUrl.substring(idx + marker.length());

            String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + objectPath;

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseAnonKey);
            headers.set("Authorization", "Bearer " + supabaseAnonKey);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);
            log.info("File deleted from Supabase: {}", objectPath);

        } catch (Exception e) {
            log.error("Failed to delete file from Supabase: {}", e.getMessage());
        }
    }
}
