package com.saas.saas_foundry_api.service.queue;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserProvisioningEvent implements QueueEvent {

  private long id;
  private String clientUid;

  UserProvisioningEvent() {
    super();
  }

  @Override
  public String toString() {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(Map.of("id", id, "clientUid", clientUid));
    } catch (Exception e) {
      return "{}";
    }
  }
}
