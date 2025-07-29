package com.saas.saas_foundry_api.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.saas.saas_foundry_api.config.properties.KeycloakProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class KeycloakAdminConfig {

  private final KeycloakProperties keycloakProperties;

  @Bean
  public Keycloak keycloakAdminClient() {
    return KeycloakBuilder.builder()
        .serverUrl(keycloakProperties.getUrl())
        .realm("master")
        .clientId(keycloakProperties.getClientId())
        .clientSecret(keycloakProperties.getClientSecret())
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .build();
  }
}