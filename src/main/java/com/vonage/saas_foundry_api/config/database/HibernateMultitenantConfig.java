package com.vonage.saas_foundry_api.config.database;

import org.hibernate.cfg.JdbcSettings;
import org.hibernate.cfg.MultiTenancySettings;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import lombok.RequiredArgsConstructor;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class HibernateMultitenantConfig {

  private final DataSource defaultDataSource;
  private final MultiTenantConnectionProviderImpl multiTenantConnectionProvider;
  private final CurrentTenantIdentifierResolverImpl currentTenantIdentifierResolver;

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
    Map<String, Object> props = new HashMap<>();
    props.put(MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
    props.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
    props.put(JdbcSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");

    return builder
        .dataSource(defaultDataSource)
        .packages("com.vonage.saas_foundry_api.database.entity")
        .properties(props)
        .build();
  }
}
