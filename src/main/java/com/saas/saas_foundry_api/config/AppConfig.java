package com.saas.saas_foundry_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.saas.saas_foundry_api.config.database.TenantRepositoryExecutor;
import jakarta.persistence.EntityManagerFactory;

@Configuration
public class AppConfig {

  @Bean
  public TenantRepositoryExecutor tenantRepositoryExecutor(EntityManagerFactory entityManagerFactory) {
    return new TenantRepositoryExecutor(entityManagerFactory);
  }
}
