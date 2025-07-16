package com.vonage.saas_foundry_api.config.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
public class DataSourceConfig {

  @Bean
  public DataSourceManager dataSourceManager() {
    return new DataSourceManager();
  }

  @Bean
  public DataSource dataSource(@Autowired DataSourceManager manager) {
    DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource();
    routingDataSource.setTargetDataSources(new HashMap<>()); // We override this dynamically.
    routingDataSource.setDefaultTargetDataSource(manager.getOrCreateDataSource("saas")); // Optional
    return routingDataSource;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      EntityManagerFactoryBuilder builder, DataSource dataSource) {
    return builder
        .dataSource(dataSource)
        .packages("com.vonage.saas_foundry_api.database.entity")
        .persistenceUnit("default")
        .build();
  }

  @Bean
  public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean factoryBean) {
    return new JpaTransactionManager(factoryBean.getObject());
  }
}
