package com.library.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.library.entity.Category;
import com.library.entity.User;
import com.library.repository.CategoryRepository;
import com.library.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    // Native Java Logger instead of Lombok @Slf4j
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    // Manual constructor injection instead of Lombok @RequiredArgsConstructor
    public DataInitializer(UserRepository userRepository, 
                           CategoryRepository categoryRepository, 
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create admin if it doesn't exist
        if (!userRepository.existsByEmail("admin@gmail.com")) {
            // Standard Java setter instantiation instead of Lombok User.builder()
            User admin = new User();
            admin.setFullName("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin@123"));
            admin.setRole(User.Role.ROLE_ADMIN);
            admin.setEnabled(true);
            
            userRepository.save(admin);
            log.info("Admin user created: admin@library.com / admin123");
        }

        // Create default categories
        List<String> defaultCategories = List.of(
            "Fiction", "Non-Fiction", "Science", "Technology",
            "History", "Biography", "Self-Help", "Business",
            "Art & Design", "Philosophy", "Psychology", "Education"
        );

        for (String name : defaultCategories) {
            if (!categoryRepository.existsByName(name)) {
                // Standard Java setter instantiation instead of Lombok Category.builder()
                Category category = new Category();
                category.setName(name);
                
                categoryRepository.save(category);
            }
        }
        log.info("Default categories initialized");
    }
}