package com.saas.saas_foundry_api.utils;

import com.saas.saas_foundry_api.context.RequestContext;
import com.saas.saas_foundry_api.enums.TenantType;
import jakarta.ws.rs.ForbiddenException;

public class TenantUtils {

  private TenantUtils() {
    // Do nothing
  }

  public static String getTenantDatabaseName(String dbName, TenantType tenantType) {
    return (tenantType.getValue() + "_" + dbName)
        .replaceAll("[^a-zA-Z0-9_]", "_")
        .toLowerCase();
  }

  public static void isOrganizationAuth(RequestContext requestContext) {
    if (!requestContext.getType().equals(TenantType.ORGANIZATION)) {
      throw new ForbiddenException();
    }
  }

  public static void isClientAuth(RequestContext requestContext) {
    if (!requestContext.getType().equals(TenantType.CLIENT)) {
      throw new ForbiddenException();
    }
  }

  public static void isSuperAdminAuth(RequestContext requestContext) {
    if (!requestContext.getType().equals(TenantType.SUPER_ADMIN)) {
      throw new ForbiddenException();
    }
  }
}
