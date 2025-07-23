package com.vonage.saas_foundry_api.service.worker;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vonage.saas_foundry_api.common.QueueNames;
import com.vonage.saas_foundry_api.config.database.TenantQueryRunner;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.dto.request.KeycloakRealmDto;
import com.vonage.saas_foundry_api.dto.request.KeycloakUserDto;
import com.vonage.saas_foundry_api.dto.request.SendEmailDto;
import com.vonage.saas_foundry_api.enums.TenantType;
import com.vonage.saas_foundry_api.mapper.ClientMapper;
import com.vonage.saas_foundry_api.service.other.DatabaseService;
import com.vonage.saas_foundry_api.service.other.EmailService;
import com.vonage.saas_foundry_api.service.other.TenantDbMigrationService;
import com.vonage.saas_foundry_api.service.other.KeycloakService;
import com.vonage.saas_foundry_api.service.queue.ClientProvisioningEvent;
import com.vonage.saas_foundry_api.utils.ClientUtils;
import com.vonage.saas_foundry_api.utils.TenantUtils;
import com.vonage.saas_foundry_api.utils.ThreadUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ClientProvisioningWorker {

  private final TenantQueryRunner tenantQueryRunner;
  private final KeycloakService keycloakService;
  private final EmailService emailService;
  private final DatabaseService databaseService;
  private final ClientUtils clientUtils;
  private final TenantDbMigrationService tenantDbMigrationService;
  private static final Logger logger = LoggerFactory.getLogger(ClientProvisioningWorker.class);

  @RabbitListener(queues = QueueNames.CLIENT_PROVISIONING_QUEUE)
  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
  public void provisionClient(String json) throws JsonProcessingException {
    ClientProvisioningEvent event = ClientMapper.toProvisioningEvent(json);

    String orgTenantName = TenantUtils.getTenantDatabaseName(event.getOrgUid(), TenantType.ORGANIZATION);
    ClientEntity clientEntity = clientUtils.findClientByUid(orgTenantName, event.getUid());

    if (!clientEntity.isKeycloakRealmProvisioned()) {
      createKeycloakRealm(orgTenantName, clientEntity);
    }

    if (!clientEntity.isKeycloakUserProvisioned()) {
      ThreadUtils.sleep(10000, "Sleeping for 10 sec before creating user " + clientEntity.getAdminEmail()
          + " for realm " + clientEntity.getUid());
      createKeycloakUser(orgTenantName, clientEntity);
    }

    if (!clientEntity.isDbProvisioned()) {
      createDatabase(orgTenantName, clientEntity);
    }

    if (!clientEntity.isWelcomeEmailSent()) {
      sendWelcomeEmail(orgTenantName, clientEntity);
    }
  }

  private void createKeycloakRealm(String orgTenantName, ClientEntity clientEntity) {
    boolean isCreated = keycloakService.createRealm(new KeycloakRealmDto(clientEntity.getUid(), clientEntity.getName()));
    clientEntity.setKeycloakRealmProvisioned(isCreated);
    clientEntity.setKeycloakRealmProvisionAttemptedOn(Instant.now());

    updateClientEntity(orgTenantName, clientEntity);

    if (!isCreated) {
      throw new CannotCreateTransactionException("Could not create a Keycloak Realm");
    }
  }

  private void createKeycloakUser(String orgTenantName, ClientEntity clientEntity) {
    KeycloakUserDto keycloakUserDto = KeycloakUserDto.builder()
        .username(clientEntity.getAdminEmail())
        .email(clientEntity.getAdminEmail())
        .firstName("Admin")
        .lastName("Admin")
        .realm(clientEntity.getUid())
        .build();

    boolean isCreated = keycloakService.createUser(keycloakUserDto);
    clientEntity.setKeycloakUserProvisioned(isCreated);
    clientEntity.setKeycloakUserProvisionAttemptedOn(Instant.now());

    updateClientEntity(orgTenantName, clientEntity);

    if (!isCreated) {
      throw new CannotCreateTransactionException("Could not create a client admin user in Keycloak");
    }
  }

  private void sendWelcomeEmail(String orgTenantName, ClientEntity clientEntity) {
    boolean isSent = emailService.sendEmail(new SendEmailDto(clientEntity.getAdminEmail(), "Hello World"));

    clientEntity.setWelcomeEmailSent(isSent);
    clientEntity.setWelcomeEmailAttemptedOn(Instant.now());

    updateClientEntity(orgTenantName, clientEntity);

    if (!isSent) {
      throw new CannotCreateTransactionException("Could not send welcome email to user");
    }
  }
  
  private void createDatabase(String orgTenantName, ClientEntity clientEntity) {
    String dbName = TenantUtils.getTenantDatabaseName(clientEntity.getUid(), TenantType.CLIENT);
    databaseService.createDatabase(dbName);
    clientEntity.setDbProvisioned(true);
    clientEntity.setDbProvisionAttemptedOn(Instant.now());

    ThreadUtils.sleep(10000, "Sleeping for 5 seconds before migrating schema to the new database.");

    tenantDbMigrationService.migrate(dbName, TenantType.CLIENT);

    updateClientEntity(orgTenantName, clientEntity);
  }

  private void updateClientEntity(String orgTenantName, ClientEntity clientEntity) {
    tenantQueryRunner.runInTenant(orgTenantName, entityManager -> {
      entityManager.merge(clientEntity);
      return null;
    });
  }

  @Recover
  public void recover(Exception ex, String json) {
    logger.error("Final failure after retries for event: {}", json, ex);
  }
}