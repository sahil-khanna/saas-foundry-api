package com.vonage.saas_foundry_api.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KeycloakUserDto {
  private String email;
  private String username;
  private String realm;
  private String firstName;
  private String lastName;
}
