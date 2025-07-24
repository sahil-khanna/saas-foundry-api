package com.saas.saas_foundry_api.service.other;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.saas.saas_foundry_api.config.properties.DatabaseProperties;
import com.saas.saas_foundry_api.enums.TenantType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TenantDbMigrationService {

  private final DatabaseProperties databaseProperties;

  private static final Logger logger = LoggerFactory.getLogger(TenantDbMigrationService.class);

  public void migrate(String dbName, TenantType tenantType) {
    logger.info("Running Flyway migration for DB: {}", dbName);

    Flyway flyway = Flyway.configure()
        .dataSource(databaseProperties.getUrl() + "/" + dbName, databaseProperties.getUsername(), databaseProperties.getPassword())
        .locations("classpath:db/migration/" + tenantType.getValue())
        .baselineOnMigrate(true)
        .load();

    try {
      flyway.migrate();
    } catch (Exception e) {
      logger.error("Failed to migrate schema to the database {}: {}", dbName, e.getMessage());
      throw e;
    }
  }
}