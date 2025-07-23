package com.vonage.saas_foundry_api.service.other;

import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vonage.saas_foundry_api.config.properties.DatabaseProperties;
import com.vonage.saas_foundry_api.config.properties.TenantProperties;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class DatabaseService {

  private final DatabaseProperties databaseProperties;
  private final TenantProperties tenantProperties;
  private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

  public void createDatabase(String dbName) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(databaseProperties.getUrl() + "/" + tenantProperties.getRoot());
    dataSource.setUsername(databaseProperties.getUsername());
    dataSource.setPassword(databaseProperties.getPassword());

    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM pg_database WHERE datname = ?",
        Integer.class,
        dbName);
    boolean exists = count != null && count > 0;

    if (!exists) {
      jdbcTemplate.execute("CREATE DATABASE " + dbName);
      logger.info("Database {} created.", dbName);
    } else {
      logger.error("Database {} already exists.", dbName);
    }
  }
}