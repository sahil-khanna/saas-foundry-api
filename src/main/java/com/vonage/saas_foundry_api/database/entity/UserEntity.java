package com.vonage.saas_foundry_api.database.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity extends EntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(length = 50, name = "first_name")
  private String firstName;

  @Column(length = 50, name = "last_name")
  private String lastName;

  @Column(length = 150)
  private String email;

  @Column(name = "is_keycloak_user_provisioned")
  private boolean isKeycloakUserProvisioned;

  @Column(name = "keycloak_user_provision_attempted_on")
  private Instant keycloakUserProvisionAttemptedOn;

  @Column(name = "is_welcome_email_sent")
  private boolean isWelcomeEmailSent;

  @Column(name = "welcome_email_attempted_on")
  private Instant welcomeEmailAttemptedOn;
}