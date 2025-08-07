package com.saas.saas_foundry_api.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.saas_foundry_api.database.entity.OrganizationEntity;
import com.saas.saas_foundry_api.dto.request.OrganizationDto;
import com.saas.saas_foundry_api.service.queue.TenantProvisioningEvent;

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

  public static TenantProvisioningEvent toProvisioningEvent(String json)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(json,
        TenantProvisioningEvent.class);
  }

  public static String toJsonString(TenantProvisioningEvent organizationProvisioningEvent)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(organizationProvisioningEvent);
  }
}
