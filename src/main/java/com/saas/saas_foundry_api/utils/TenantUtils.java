package com.saas.saas_foundry_api.utils;

import com.saas.saas_foundry_api.enums.TenantType;

public class TenantUtils {

  private TenantUtils() {
    // Do nothing
  }

  public static String getTenantDatabaseName(String dbName, TenantType tenantType) {
    return (tenantType.getValue() + "_" + dbName)
        .replaceAll("[^a-zA-Z0-9_]", "_")
        .toLowerCase();
  }
}
