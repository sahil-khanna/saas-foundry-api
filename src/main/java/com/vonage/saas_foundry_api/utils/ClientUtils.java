package com.vonage.saas_foundry_api.utils;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.vonage.saas_foundry_api.config.database.TenantQueryRunner;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ClientUtils {

  private final TenantQueryRunner tenantQueryRunner;

  public ClientEntity findClientByUid(String tenantName, String clientUid) {
    return tenantQueryRunner.runInTenant(
        tenantName,
        entityManager -> Optional.ofNullable(entityManager.find(ClientEntity.class, clientUid))
            .orElseThrow(() -> new NotFoundException("Client not found")));
  }
}