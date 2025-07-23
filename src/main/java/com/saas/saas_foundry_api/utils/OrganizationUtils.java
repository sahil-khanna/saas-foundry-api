package com.saas.saas_foundry_api.utils;

import org.springframework.stereotype.Service;

import com.saas.saas_foundry_api.config.database.TenantQueryRunner;
import com.saas.saas_foundry_api.config.properties.TenantProperties;
import com.saas.saas_foundry_api.database.entity.OrganizationEntity;
import com.saas.saas_foundry_api.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OrganizationUtils {

  private final TenantProperties tenantProperties;
  private final TenantQueryRunner tenantQueryRunner;
  
  public OrganizationEntity findOrgByUid(String orgUid) {
    return tenantQueryRunner.runInTenant(tenantProperties.getRoot(), entityManager -> entityManager.createQuery(
        "FROM OrganizationEntity o WHERE o.uid = :uid", OrganizationEntity.class)
        .setParameter("uid", orgUid)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found")));
  }
}
