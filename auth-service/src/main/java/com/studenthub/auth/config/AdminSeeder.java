package com.studenthub.auth.config;

import com.studenthub.auth.entities.User;
import com.studenthub.auth.enums.UserRole;
import com.studenthub.auth.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@studenthub.com";

            if (userRepository.existsByEmail(adminEmail)) {
                return; // already seeded
            }

            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123")); // or env var
            admin.setRole(UserRole.ADMIN);

            userRepository.save(admin);

            System.out.println("✅ Admin user created: admin@studenthub.com / admin123");
        };
    }
}
