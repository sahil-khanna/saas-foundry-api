package com.saas.saas_foundry_api.service.other;

import java.util.List;
import org.apache.http.HttpStatus;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.saas.saas_foundry_api.dto.request.KeycloakRealmDto;
import com.saas.saas_foundry_api.dto.request.KeycloakUserDto;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class KeycloakService {

  private final Keycloak keycloak;
  private static final Logger logger = LoggerFactory.getLogger(KeycloakService.class);

  public boolean createUser(KeycloakUserDto keycloakUserDto) {
    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentation.setUsername(keycloakUserDto.getUsername());
    userRepresentation.setEmail(keycloakUserDto.getEmail());
    userRepresentation.setFirstName(keycloakUserDto.getFirstName());
    userRepresentation.setLastName(keycloakUserDto.getLastName());
    userRepresentation.setEnabled(true);

    CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
    credentialRepresentation.setTemporary(false);
    credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
    // TODO: To be removed when email is integrated in Keycloak.
    credentialRepresentation.setValue("123456789");

    userRepresentation.setCredentials(List.of(credentialRepresentation));

    boolean status = false;

    logger.info("Creating user {} in realm {}", userRepresentation.getEmail(), keycloakUserDto.getRealm());
    try (Response response = keycloak.realm(keycloakUserDto.getRealm()).users().create(userRepresentation)) {
      status = response.getStatus() == HttpStatus.SC_CREATED;
      if (status) {
        logger.info("Created user {} in realm {}", userRepresentation.getEmail(), keycloakUserDto.getRealm());
      } else {
        logger.info("Failed to create user {} in realm {}. Error: {}", userRepresentation.getEmail(),
            keycloakUserDto.getRealm(), response.getStatusInfo().getReasonPhrase());
      }

    } catch (Exception e) {
      logger.error("Failed to create user {} in realm {}: {}", keycloakUserDto.getEmail(), keycloakUserDto.getRealm(),
          e.getMessage());
    }

    return status;
  }

  public boolean createRealm(KeycloakRealmDto keycloakRealmDto) {
    RealmRepresentation realmRepresentation = new RealmRepresentation();
    realmRepresentation.setDisplayName(keycloakRealmDto.getDisplayName());
    realmRepresentation.setRealm(keycloakRealmDto.getUid());
    realmRepresentation.setEnabled(true);
    realmRepresentation.setAccessTokenLifespan(900);

    ClientRepresentation clientRepresentation = new ClientRepresentation();
    clientRepresentation.setClientId("saas-user");
    clientRepresentation.setEnabled(true);
    clientRepresentation.setRedirectUris(List.of("http://localhost:3000/*"));
    clientRepresentation.setServiceAccountsEnabled(true);
    clientRepresentation.setPublicClient(true);
    clientRepresentation.setDirectAccessGrantsEnabled(true);
    realmRepresentation.setClients(List.of(clientRepresentation));

    boolean status = false;

    logger.info("Creating realm {} and client {}", realmRepresentation.getRealm(), clientRepresentation.getClientId());
    try {
      keycloak.realms().create(realmRepresentation);
      status = true;
      logger.info("Created realm {} and client {}", realmRepresentation.getRealm(), clientRepresentation.getClientId());
    } catch (Exception e) {
      logger.error("Failed to create realm {} and client {}: {}", realmRepresentation.getRealm(),
          clientRepresentation.getClientId(),
          e.getMessage());
    }

    return status;
  }
}