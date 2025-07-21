package com.vonage.saas_foundry_api.config.database;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String> {

  @Value("${tenant.root}")
  private String rootTenant;

  @Override
  public String resolveCurrentTenantIdentifier() {
    String tenantId = TenantContext.getTenantId();
    return (tenantId != null) ? tenantId : rootTenant;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
