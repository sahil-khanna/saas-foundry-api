package com.vonage.saas_foundry_api.config.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DataSourceManager {

  @Value("${postgres.url}")
  private String postgresUrl;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  @Value("${spring.datasource.driver-class-name}")
  private String driverClassName;

  private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

  public DataSource getOrCreateDataSource(String dbName) {
    return dataSources.computeIfAbsent(dbName, this::createDataSourceForDb);
  }

  private DataSource createDataSourceForDb(String dbName) {
    log.info("Creating new DataSource for DB: {}", dbName);
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(postgresUrl + "/" + dbName);
    config.setUsername(username);
    config.setPassword(password);
    config.setDriverClassName(driverClassName);
    config.setMaximumPoolSize(10);
    config.setPoolName("Hikari-" + dbName);
    return new HikariDataSource(config);
  }
}
