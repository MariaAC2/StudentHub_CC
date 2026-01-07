package com.studenthub.business.config;

import com.studenthub.business.entities.User;
import com.studenthub.business.enums.UserRole;
import com.studenthub.business.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmins(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {

        return args -> {

            List<AdminSeedData> admins = List.of(
                    new AdminSeedData("Admin1", "admin1@studenthub.com"),
                    new AdminSeedData("Admin2", "admin2@studenthub.com"),
                    new AdminSeedData("Admin3", "admin3@studenthub.com"),
                    new AdminSeedData("Admin4", "admin4@studenthub.com"),
                    new AdminSeedData("Admin5", "admin5@studenthub.com")
            );

            for (AdminSeedData admin : admins) {

                boolean exists = userRepository.findByEmail(admin.email()).isPresent();

                if (!exists) {
                    User user = new User(
                            admin.name(),
                            admin.email(),
                            passwordEncoder.encode("ChangeMe123!"),
                            UserRole.ADMIN
                    );

                    userRepository.save(user);
                }
            }
        };
    }

    // small internal record for clarity
    private record AdminSeedData(String name, String email) {}
}