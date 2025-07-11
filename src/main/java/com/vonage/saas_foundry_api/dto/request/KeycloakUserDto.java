package com.vonage.saas_foundry_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class KeycloakUserDto {
  private String email;
  private String username;
  private String realm;
}
