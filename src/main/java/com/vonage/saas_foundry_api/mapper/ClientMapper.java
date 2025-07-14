package com.vonage.saas_foundry_api.mapper;

import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;
import com.vonage.saas_foundry_api.dto.request.ClientDto;

public class ClientMapper {

  private ClientMapper() {
    // Do nothing
  }

  public static ClientEntity toEntity(OrganizationEntity organizationEntity, ClientDto clientDto) {
    ClientEntity clientEntity = new ClientEntity();
    clientEntity.setName(clientDto.getName());
    clientEntity.setAdminEmail(clientDto.getAdminEmail());
    clientEntity.setOrganization(organizationEntity);

    return clientEntity;
  }
  
  public static ClientDto toDto(ClientEntity clientEntity) {
    ClientDto clientDto = new ClientDto();
    clientDto.setAdminEmail(clientEntity.getAdminEmail());
    clientDto.setName(clientEntity.getName());
    clientDto.setUid(clientEntity.getUid());
    return clientDto;
  }
}
