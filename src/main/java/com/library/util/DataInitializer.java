package com.library.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.library.entity.Category;
import com.library.entity.User;
import com.library.repository.CategoryRepository;
import com.library.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createUploadDirectories();
        createAdminUser();
        createDefaultCategories();
    }

    private void createUploadDirectories() {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath.resolve("pdfs"));
            Files.createDirectories(uploadPath.resolve("covers"));
            Files.createDirectories(uploadPath.resolve("profiles"));
            log.info("Upload directories created at: {}", uploadPath);
        } catch (Exception e) {
            log.error("Failed to create upload directories: {}", e.getMessage());
        }
    }

    private void createAdminUser() {
        if (!userRepository.existsByEmail("admin@library.com")) {
            User admin = new User();
            admin.setFullName("Admin");
            admin.setEmail("admin@library.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ROLE_ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("Admin user created: admin@library.com / admin123");
        }
    }

    private void createDefaultCategories() {
        List<String> defaultCategories = List.of(
            "Fiction", "Non-Fiction", "Science", "Technology",
            "History", "Biography", "Self-Help", "Business",
            "Art & Design", "Philosophy", "Psychology", "Education"
        );

        for (String name : defaultCategories) {
            if (!categoryRepository.existsByName(name)) {
                Category category = new Category();
                category.setName(name);
                categoryRepository.save(category);
            }
        }
        log.info("Default categories initialized");
    }
}
