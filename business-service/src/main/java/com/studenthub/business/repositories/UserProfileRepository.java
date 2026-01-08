package com.studenthub.business.repositories;

import com.studenthub.business.entities.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Page<UserProfile> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable);

    Optional<UserProfile> findByName(String name);
}