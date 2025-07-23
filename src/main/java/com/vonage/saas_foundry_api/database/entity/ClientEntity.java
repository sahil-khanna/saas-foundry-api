package com.vonage.saas_foundry_api.database.entity;

import java.time.Instant;
import com.github.ksuid.Ksuid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Index;

@Getter
@Setter
@Entity
@Table(name = "clients", indexes = {
    @Index(name = "idx_client_name", columnList = "name")
})
public class ClientEntity extends EntityBase {

  @Id
  @Column(name = "id", length = 27, nullable = false, updatable = false, unique = true)
  private final String uid = Ksuid.newKsuid().toString();

  @Column(length = 250)
  private String name;

  @Column(length = 150)
  private String adminEmail;

  @Column(name = "is_db_provisioned")
  private boolean isDbProvisioned = false;

  @Column(name = "db_provision_attempted_on")
  private Instant dbProvisionAttemptedOn;

  @Column(name = "is_keycloak_realm_provisioned")
  private boolean isKeycloakRealmProvisioned;

  @Column(name = "keycloak_realm_provision_attempted_on")
  private Instant keycloakRealmProvisionAttemptedOn;

  @Column(name = "is_keycloak_user_provisioned")
  private boolean isKeycloakUserProvisioned;

  @Column(name = "keycloak_user_provision_attempted_on")
  private Instant keycloakUserProvisionAttemptedOn;

  @Column(name = "is_welcome_email_sent")
  private boolean isWelcomeEmailSent;

  @Column(name = "welcome_email_attempted_on")
  private Instant welcomeEmailAttemptedOn;
}