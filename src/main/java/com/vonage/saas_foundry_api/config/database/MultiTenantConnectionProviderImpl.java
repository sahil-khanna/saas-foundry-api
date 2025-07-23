package com.vonage.saas_foundry_api.config.database;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider, Serializable {

  private static final long serialVersionUID = 1L;

  private static final String BASE_URL = "jdbc:postgresql://localhost:5432/";
  private static final String USERNAME = "postgres";
  private static final String PASSWORD = "sliyRR@fPdsf3";

  private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

  private DataSource getDataSource(String tenantId) {
    return dataSourceMap.computeIfAbsent(tenantId, id -> {
      DriverManagerDataSource ds = new DriverManagerDataSource();
      ds.setDriverClassName("org.postgresql.Driver");
      ds.setUrl(BASE_URL + id);
      ds.setUsername(USERNAME);
      ds.setPassword(PASSWORD);
      return ds;
    });
  }

  @Override
  public Connection getConnection(Object tenantIdentifier) throws SQLException {
    System.out.println("🔄 Switching to tenant DB: " + tenantIdentifier);
    return getDataSource(tenantIdentifier.toString()).getConnection();
  }

  @Override
  public Connection getAnyConnection() throws SQLException {
    return getDataSource("saas").getConnection(); // default tenant DB
  }

  @Override
  public void releaseAnyConnection(Connection connection) throws SQLException {
    connection.close();
  }

  @Override
  public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
    connection.close();
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return false;
  }

  @Override
  public boolean isUnwrappableAs(Class<?> unwrapType) {
    return MultiTenantConnectionProvider.class.equals(unwrapType) ||
        MultiTenantConnectionProviderImpl.class.isAssignableFrom(unwrapType);
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    if (isUnwrappableAs(unwrapType)) {
      return unwrapType.cast(this);
    }
    throw new IllegalArgumentException("Cannot unwrap to: " + unwrapType);
  }
}
