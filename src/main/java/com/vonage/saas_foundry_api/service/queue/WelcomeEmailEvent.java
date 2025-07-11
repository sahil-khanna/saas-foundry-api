package com.vonage.saas_foundry_api.service.queue;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WelcomeEmailEvent implements QueueEvent {

  private String email;
  private String body;

  @Override
  public String toString() {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(Map.of("email", email, "body", body));
    } catch (Exception e) {
      return "{}";
    }
  }
}
