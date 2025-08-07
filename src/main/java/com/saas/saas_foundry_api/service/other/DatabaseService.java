package com.saas.saas_foundry_api.service.other;

import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.saas.saas_foundry_api.config.properties.DatabaseProperties;
import com.saas.saas_foundry_api.config.properties.TenantProperties;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class DatabaseService {

  private final DatabaseProperties databaseProperties;
  private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

  public void createDatabase(String schemaName) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(databaseProperties.getUrl());
    dataSource.setUsername(databaseProperties.getUsername());
    dataSource.setPassword(databaseProperties.getPassword());

    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?",
        Integer.class,
        schemaName);
    boolean exists = count != null && count > 0;

    if (!exists) {
      jdbcTemplate.execute("CREATE SCHEMA \"" + schemaName + "\"");
      logger.info("Schema {} created.", schemaName);
    } else {
      logger.error("Schema {} already exists.", schemaName);
    }

    dataSource.close();
  }
}