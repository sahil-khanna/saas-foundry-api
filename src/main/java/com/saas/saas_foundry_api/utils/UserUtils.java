package com.saas.saas_foundry_api.utils;

import java.util.Optional;
import org.springframework.stereotype.Service;

import com.saas.saas_foundry_api.config.database.TenantQueryRunner;
import com.saas.saas_foundry_api.database.entity.UserEntity;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserUtils {

  private final TenantQueryRunner tenantQueryRunner;

  public UserEntity findUserById(String tenantName, long userId) {
    return tenantQueryRunner.runInTenant(
        tenantName,
        entityManager -> Optional.ofNullable(entityManager.find(UserEntity.class, userId))
            .orElseThrow(() -> new NotFoundException("User not found")));
  }
}