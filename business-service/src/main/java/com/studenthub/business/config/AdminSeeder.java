package com.studenthub.business.config;

import com.studenthub.business.dtos.RegisterRequest;
import com.studenthub.business.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserService userService) {
        return args -> {

            String adminEmail = "admin@studenthub.com";
            String adminPassword = "admin123";

            try {
                System.out.println("=== [ADMIN SEEDER] START ===");

                RegisterRequest req = new RegisterRequest(
                        "Admin",
                        adminEmail,
                        adminPassword,
                        false // requestTeacher
                );

                userService.registerWithRole(req, "ADMIN");

                System.out.println("✅ [ADMIN SEEDER] Admin created successfully");
                System.out.println("   email: " + adminEmail);
                System.out.println("   password: " + adminPassword);

            } catch (Exception e) {
                // Most likely: admin already exists
                System.out.println("ℹ️ [ADMIN SEEDER] Admin already exists or seeding skipped");
                System.out.println("   reason: " + e.getMessage());
            }
        };
    }
}
