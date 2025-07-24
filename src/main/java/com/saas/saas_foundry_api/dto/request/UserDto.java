package com.saas.saas_foundry_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "User")
public class UserDto {
  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  private long id;
  private String firstName;
  private String lastName;
  private String email;
}
