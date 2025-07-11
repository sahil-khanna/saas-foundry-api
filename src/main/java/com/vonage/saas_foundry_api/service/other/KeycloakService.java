package com.vonage.saas_foundry_api.service.other;

import org.apache.http.HttpStatus;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import com.vonage.saas_foundry_api.dto.request.KeycloakRealmDto;
import com.vonage.saas_foundry_api.dto.request.KeycloakUserDto;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class KeycloakService {

  private final Keycloak keycloak;
  
  public boolean createUser(KeycloakUserDto keycloakUserDto) {
    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentation.setUsername(keycloakUserDto.getUsername());
    userRepresentation.setEmail(keycloakUserDto.getEmail());
    userRepresentation.setEnabled(true);

    Response response = keycloak.realm(keycloakUserDto.getRealm()).users().create(userRepresentation);
    boolean status = response.getStatus() == HttpStatus.SC_CREATED;
    response.close();

    return status;
  }

  public boolean createRealm(KeycloakRealmDto keycloakRealmDto) {
    RealmRepresentation realmRepresentation = new RealmRepresentation();
    realmRepresentation.setDisplayName(keycloakRealmDto.getDisplayName());
    realmRepresentation.setRealm(keycloakRealmDto.getUid());
    realmRepresentation.setEnabled(true);

    keycloak.realms().create(realmRepresentation);
    return true;
  }
}