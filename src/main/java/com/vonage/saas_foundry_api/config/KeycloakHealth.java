package com.vonage.saas_foundry_api.config;

import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class KeycloakHealth implements HealthIndicator {

  private final Keycloak keycloak;

  @Override
  public Health health() {
    try {
      String version = keycloak.serverInfo().getInfo().getSystemInfo().getVersion();
      return Health.up().withDetail("version", version).build();
    } catch (Exception e) {
      return Health.down().withDetail("version", "unavailable").build();
    }
  }
}
