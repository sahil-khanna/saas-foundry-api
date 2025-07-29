package com.saas.saas_foundry_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.saas.saas_foundry_api.config.database.TenantQueryRunner;
import jakarta.persistence.EntityManagerFactory;

@Configuration
public class AppConfig {
  
  @Bean
  public TenantQueryRunner tenantQueryRunner(EntityManagerFactory entityManagerFactory) {
    return new TenantQueryRunner(entityManagerFactory);
  }
}
