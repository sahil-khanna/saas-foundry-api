package com.saas.saas_foundry_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "Organization")
public class OrganizationDto {
  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  private String uid;
  private String name;
  private String adminEmail;
}
