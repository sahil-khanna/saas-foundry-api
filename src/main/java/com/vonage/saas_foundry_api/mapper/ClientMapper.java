package com.vonage.saas_foundry_api.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.dto.request.ClientDto;
import com.vonage.saas_foundry_api.service.queue.ClientProvisioningEvent;

public class ClientMapper {

  private ClientMapper() {
    // Do nothing
  }

  public static ClientEntity toEntity(ClientDto clientDto) {
    ClientEntity clientEntity = new ClientEntity();
    clientEntity.setName(clientDto.getName());
    clientEntity.setAdminEmail(clientDto.getAdminEmail());

    return clientEntity;
  }
  
  public static ClientDto toDto(ClientEntity clientEntity) {
    ClientDto clientDto = new ClientDto();
    clientDto.setAdminEmail(clientEntity.getAdminEmail());
    clientDto.setName(clientEntity.getName());
    clientDto.setUid(clientEntity.getUid());
    return clientDto;
  }

  public static ClientProvisioningEvent toProvisioningEvent(String json)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(json,
        ClientProvisioningEvent.class);
  }

  public static String toJsonString(ClientProvisioningEvent clientProvisioningEvent)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(clientProvisioningEvent);
  }
}
