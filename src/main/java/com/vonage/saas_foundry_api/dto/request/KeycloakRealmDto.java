package com.vonage.saas_foundry_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KeycloakRealmDto {
  private String uid;
  private String displayName;
}
