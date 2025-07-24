package com.saas.saas_foundry_api.dto.response;

import java.util.List;

import com.saas.saas_foundry_api.dto.request.OrganizationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "Organizations")
public class OrganizationsDto {
  private List<OrganizationDto> organizations;
  private long total;
}
