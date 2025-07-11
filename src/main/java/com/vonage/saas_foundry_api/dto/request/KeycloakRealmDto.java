package com.vonage.saas_foundry_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class KeycloakRealmDto {
  private String uid;
  private String displayName;
}
