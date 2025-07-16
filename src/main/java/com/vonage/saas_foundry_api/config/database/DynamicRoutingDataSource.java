package com.vonage.saas_foundry_api.config.database;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

  @Override
  protected Object determineCurrentLookupKey() {
    return DatabaseContextHolder.getCurrentDb();
  }
}
