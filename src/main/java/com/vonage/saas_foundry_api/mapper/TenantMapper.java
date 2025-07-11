package com.vonage.saas_foundry_api.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vonage.saas_foundry_api.service.queue.TenantProvisioningEvent;

public class TenantMapper {

  private TenantMapper() {
    // Do nothing
  }

  public static TenantProvisioningEvent toTenantProvisioningEvent(String json)
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
