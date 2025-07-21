package com.vonage.saas_foundry_api.service.other;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TenantDbMigrationService {

  @Value("${spring.datasource.url}")
  private String postgresUrl;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  private static final Logger logger = LoggerFactory.getLogger(TenantDbMigrationService.class);

  public void migrate(String dbName) {
    logger.info("Running Flyway migration for DB: {}", dbName);

    Flyway flyway = Flyway.configure()
        .dataSource(postgresUrl + "/" + dbName, username, password)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .load();

    flyway.migrate();
  }
}