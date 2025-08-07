package com.saas.saas_foundry_api.config.database;

import java.util.Map;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import com.saas.saas_foundry_api.config.properties.TenantProperties;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {


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

  @Override
  public void customize(Map<String, Object> hibernateProperties) {
    hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
  }
}
