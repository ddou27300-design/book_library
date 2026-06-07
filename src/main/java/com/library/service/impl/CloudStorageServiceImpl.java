package com.library.service.impl;

import com.library.service.CloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class CloudStorageServiceImpl implements CloudStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudStorageServiceImpl.class);

    // កំណត់ផ្លូវទៅកាន់ថតផ្ទុក uploads នៅខាងក្រៅដើម្បីងាយស្រួលគ្រប់គ្រងទាំងលើ Local និង Render
    private final String uploadRootPath = "uploads";

    public CloudStorageServiceImpl() {
        // បង្កើតថត uploads ធំមួយបើមិនទាន់មាននៅក្នុងគម្រោង
        File rootFolder = new File(uploadRootPath);
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // ១. បង្កើតផ្លូវថតជាក់លាក់ (ឧទាហរណ៍៖ uploads/pdfs ឬ uploads/covers)
            String targetDirPath = uploadRootPath + File.separator + folder;
            File targetDir = new File(targetDirPath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            // ២. ញែកយក Extension របស់ឯកសារ (.pdf, .jpg)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // ៣. បង្កើតឈ្មោះឯកសារថ្មីមិនឱ្យជាន់គ្នាដើម្បីសុវត្ថិភាព
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path copyLocation = Paths.get(targetDirPath + File.separator + uniqueFilename);

            // ៤. ចម្លងឯកសារចូលទៅក្នុងថតម៉ាស៊ីន
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);

            // ៥. បង្កើតជា URL Path ត្រលប់ទៅកាន់ Database (ឧទាហរណ៍៖ /uploads/pdfs/abc-123.pdf)
            String fileUrl = "/uploads/" + folder + "/" + uniqueFilename;
            log.info("File uploaded successfully to Local Storage: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file locally: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // បំប្លែងពី URL API មកជាផ្លូវឯកសារពិតប្រាកដក្នុងម៉ាស៊ីនដើម្បីលុបចោល
            // ពី "/uploads/pdfs/filename.pdf" ទៅជា "uploads/pdfs/filename.pdf"
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully from Local Storage: {}", relativePath);
            } else {
                log.warn("File not found for deletion: {}", relativePath);
            }

        } catch (IOException e) {
            log.error("Failed to delete local file: {}", e.getMessage());
        }
    }
}