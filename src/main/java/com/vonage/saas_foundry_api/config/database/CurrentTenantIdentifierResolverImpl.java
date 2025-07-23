package com.vonage.saas_foundry_api.config.database;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import com.vonage.saas_foundry_api.config.properties.TenantProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String> {

  private final TenantProperties tenantProperties;

  @Override
  public String resolveCurrentTenantIdentifier() {
    String tenantId = TenantContext.getTenantId();
    return (tenantId != null) ? tenantId : tenantProperties.getRoot();
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
