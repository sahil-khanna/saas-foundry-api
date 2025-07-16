package com.vonage.saas_foundry_api.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vonage.saas_foundry_api.database.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  boolean existsByEmail(String email);
}
