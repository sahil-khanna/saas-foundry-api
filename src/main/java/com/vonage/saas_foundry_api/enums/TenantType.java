package com.vonage.saas_foundry_api.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TenantType {

  ORGANIZATION("organization"),
  CLIENT("client");

  private final String value;
}
