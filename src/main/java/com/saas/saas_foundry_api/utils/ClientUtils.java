package com.saas.saas_foundry_api.utils;

import org.springframework.stereotype.Service;
import com.saas.saas_foundry_api.config.database.TenantRepositoryExecutor;
import com.saas.saas_foundry_api.database.entity.ClientEntity;
import com.saas.saas_foundry_api.database.repository.ClientRepository;
import com.saas.saas_foundry_api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ClientUtils {

  private final TenantRepositoryExecutor tenantRepositoryExecutor;

  public ClientEntity findClientByUid(String tenantName, String clientUid) {
    return tenantRepositoryExecutor.runInTenant(
        tenantName, ClientRepository.class,
        repository -> repository.findByUid(clientUid)
            .orElseThrow(() -> new ResourceNotFoundException("Client not found")));
  }
}