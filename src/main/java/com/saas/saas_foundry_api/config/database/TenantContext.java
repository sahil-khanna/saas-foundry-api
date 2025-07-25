package com.saas.saas_foundry_api.config.database;

public class TenantContext {

  private TenantContext() {
    // Do nothing
  }

  private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

  public static void setTenantId(String tenantId) {
    CURRENT_TENANT.set(tenantId);
  }

  public static String getTenantId() {
    return CURRENT_TENANT.get();
  }

  public static void clear() {
    CURRENT_TENANT.remove();
  }
}