package com.saas.saas_foundry_api.utils;

import org.springframework.stereotype.Service;
import com.saas.saas_foundry_api.config.database.TenantRepositoryExecutor;
import com.saas.saas_foundry_api.config.properties.TenantProperties;
import com.saas.saas_foundry_api.database.entity.OrganizationEntity;
import com.saas.saas_foundry_api.database.repository.OrganizationRepository;
import com.saas.saas_foundry_api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OrganizationUtils {

  private final TenantProperties tenantProperties;
  private final TenantRepositoryExecutor tenantRepositoryExecutor;

  public OrganizationEntity findOrgByUid(String orgUid) {
    return tenantRepositoryExecutor.execute(tenantProperties.getRoot(), OrganizationRepository.class,
        repository -> repository.findByUid(orgUid)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found")));
  }
}
