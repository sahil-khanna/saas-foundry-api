package com.saas.saas_foundry_api.config.database;

import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import com.saas.saas_foundry_api.config.properties.TenantProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider, HibernatePropertiesCustomizer {

  private final DataSource dataSource;
  private final TenantProperties tenantProperties;
  private static final Logger logger = LoggerFactory.getLogger(MultiTenantConnectionProviderImpl.class);

  @SuppressWarnings("squid:S2095")
  @Override
  public Connection getConnection(Object tenantIdentifier) throws SQLException {
    String tenant = TenantContext.getTenantId();
    if (tenant == null) {
      tenant = tenantProperties.getRoot();
    }
    logger.info("Switching to tenant Schema: {}", tenant);
    Connection connection = dataSource.getConnection();
    connection.setSchema(tenant);
    return connection;
  }

  @Override
  public Connection getAnyConnection() throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public void releaseAnyConnection(Connection connection) throws SQLException {
    connection.close();
  }

  @Override
  public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
    connection.setSchema(tenantProperties.getRoot());
    connection.close();
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return false;
  }

  @Override
  public boolean isUnwrappableAs(Class<?> unwrapType) {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    throw new UnsupportedOperationException("Can't unwrap this.");
  }

  @Override
  public void customize(Map<String, Object> hibernateProperties) {
    hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
  }
}
