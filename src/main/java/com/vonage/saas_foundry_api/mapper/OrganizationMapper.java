package com.vonage.saas_foundry_api.mapper;

import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;
import com.vonage.saas_foundry_api.dto.request.OrganizationDto;

public class OrganizationMapper {

  private OrganizationMapper() {
    // Do nothing
  }

  public static OrganizationEntity toEntity(OrganizationDto organizationDto) {
    OrganizationEntity organizationEntity = new OrganizationEntity();
    organizationEntity.setName(organizationDto.getName());
    organizationEntity.setAdminEmail(organizationDto.getAdminEmail());
    return organizationEntity;
  }

  public static OrganizationDto toDto(OrganizationEntity organizationEntity) {
    OrganizationDto organizationDto = new OrganizationDto();
    organizationDto.setAdminEmail(organizationEntity.getAdminEmail());
    organizationDto.setName(organizationEntity.getName());
    organizationDto.setUid(organizationEntity.getUid());
    return organizationDto;
  }
}
