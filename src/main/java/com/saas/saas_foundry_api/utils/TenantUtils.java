package com.saas.saas_foundry_api.utils;

import java.util.List;
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

  public static void isOrganizationOrAboveAuth(RequestContext requestContext) {
    if (!List.of(TenantType.SUPER_ADMIN, TenantType.ORGANIZATION).contains(requestContext.getType())) {
      throw new ForbiddenException();
    }
  }

  public static void isClientOrAboveAuth(RequestContext requestContext) {
    if (!List.of(TenantType.SUPER_ADMIN, TenantType.ORGANIZATION, TenantType.CLIENT).contains(requestContext.getType())) {
      throw new ForbiddenException();
    }
  }

  public static void isSuperAdminAuth(RequestContext requestContext) {
    if (!requestContext.getType().equals(TenantType.SUPER_ADMIN)) {
      throw new ForbiddenException();
    }
  }
}
