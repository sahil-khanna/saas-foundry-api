package com.saas.saas_foundry_api.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TenantType {

  ORGANIZATION("organization"),
  CLIENT("client"),
  SUPER_ADMIN("super-admin");

  private final String value;
}
