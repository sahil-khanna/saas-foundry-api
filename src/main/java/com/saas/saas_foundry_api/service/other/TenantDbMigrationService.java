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

  public void migrate(String schemaName, TenantType tenantType) {
    logger.info("Running Flyway migration for DB: {}", schemaName);

    Flyway flyway = Flyway.configure()
        .dataSource(databaseProperties.getUrl(), databaseProperties.getUsername(), databaseProperties.getPassword())
        .schemas(schemaName)
        .defaultSchema(schemaName)
        .locations("classpath:db/migration/" + tenantType.getValue())
        .table("flyway_schema_history_saas_foundry_api")
        .baselineOnMigrate(true)
        .load();

    try {
      flyway.migrate();
    } catch (Exception e) {
      logger.error("Failed to migrate schema {}: {}", schemaName, e.getMessage());
      throw e;
    }
  }
}