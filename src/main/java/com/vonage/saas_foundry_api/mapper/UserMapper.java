package com.vonage.saas_foundry_api.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.database.entity.UserEntity;
import com.vonage.saas_foundry_api.dto.request.ClientDto;
import com.vonage.saas_foundry_api.dto.request.UserDto;
import com.vonage.saas_foundry_api.service.queue.UserProvisioningEvent;

public class UserMapper {

  private UserMapper() {
    // Do nothing
  }

  public static UserEntity toUserEntity(ClientEntity clientEntity, UserDto userDto) {
    UserEntity userEntity = new UserEntity();
    userEntity.setEmail(userDto.getEmail());
    userEntity.setFirstName(userDto.getFirstName());
    userEntity.setLastName(userDto.getLastName());
    userEntity.setClient(clientEntity);
    
    return userEntity;
  }

  public static UserProvisioningEvent toUserProvisioningEvent(String json)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(json,
        UserProvisioningEvent.class);
  }

  public static String toJsonString(UserProvisioningEvent userProvisioningEvent) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(userProvisioningEvent);
  }

  public static UserDto toDto(UserEntity userEntity) {
    UserDto userDto = new UserDto();
    userDto.setEmail(userEntity.getEmail());
    userDto.setFirstName(userEntity.getFirstName());
    userDto.setLastName(userEntity.getLastName());
    userDto.setId(userEntity.getId());
    return userDto;
  }
}
