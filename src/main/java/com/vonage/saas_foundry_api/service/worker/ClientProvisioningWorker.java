package com.vonage.saas_foundry_api.service.worker;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vonage.saas_foundry_api.common.QueueNames;
import com.vonage.saas_foundry_api.config.database.TenantContext;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.database.repository.ClientRepository;
import com.vonage.saas_foundry_api.dto.request.KeycloakRealmDto;
import com.vonage.saas_foundry_api.dto.request.KeycloakUserDto;
import com.vonage.saas_foundry_api.dto.request.SendEmailDto;
import com.vonage.saas_foundry_api.mapper.TenantMapper;
import com.vonage.saas_foundry_api.service.other.EmailService;
import com.vonage.saas_foundry_api.service.other.TenantDbMigrationService;
import com.vonage.saas_foundry_api.service.other.KeycloakService;
import com.vonage.saas_foundry_api.service.queue.TenantProvisioningEvent;
import com.vonage.saas_foundry_api.utils.ClientUtils;
import com.vonage.saas_foundry_api.utils.ThreadUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ClientProvisioningWorker {

  @Value("${tenant.root}")
  private String rootTenant;

  private final ClientRepository clientRepository;
  private final KeycloakService keycloakService;
  private final EmailService emailService;
  private final JdbcTemplate jdbcTemplate;
  private final ClientUtils clientUtils;
  private final TenantDbMigrationService tenantDbMigrationService;
  private static final Logger logger = LoggerFactory.getLogger(ClientProvisioningWorker.class);

  @RabbitListener(queues = QueueNames.CLIENT_PROVISIONING_QUEUE)
  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
  public void provisionClient(String json) throws JsonProcessingException {
    TenantContext.setTenantId(rootTenant);

    TenantProvisioningEvent event = TenantMapper.toTenantProvisioningEvent(json);
    ClientEntity clientEntity = clientUtils.findClientByUid(event.getUid());

    if (!clientEntity.isKeycloakRealmProvisioned()) {
      createKeycloakRealm(clientEntity);
    }

    if (!clientEntity.isDbProvisioned()) {
      createDatabase(clientEntity);
    }

    if (!clientEntity.isKeycloakUserProvisioned()) {
      ThreadUtils.sleep(10000, "Sleeping for 10 sec before creating user " + clientEntity.getAdminEmail()
          + " for realm " + clientEntity.getUid());
      createKeycloakUser(clientEntity);
    }

    if (!clientEntity.isWelcomeEmailSent()) {
      sendWelcomeEmail(clientEntity);
    }
  }

  private void createKeycloakRealm(ClientEntity clientEntity) {
    clientEntity.setKeycloakRealmProvisionAttemptedOn(Instant.now());

    boolean isCreated = keycloakService
        .createRealm(new KeycloakRealmDto(clientEntity.getUid(), clientEntity.getName()));

    if (!isCreated) {
      clientRepository.save(clientEntity);
      throw new CannotCreateTransactionException("Could not create a Keycloak Realm");
    }

    clientEntity.setKeycloakRealmProvisioned(true);
    clientRepository.save(clientEntity);
  }

  private void createKeycloakUser(ClientEntity clientEntity) {
    clientEntity.setKeycloakUserProvisionAttemptedOn(Instant.now());

    KeycloakUserDto keycloakUserDto = KeycloakUserDto.builder()
        .username(clientEntity.getAdminEmail())
        .email(clientEntity.getAdminEmail())
        .firstName("Admin")
        .lastName("Admin")
        .realm(clientEntity.getUid())
        .build();
    boolean isCreated = keycloakService.createUser(keycloakUserDto);

    if (!isCreated) {
      clientRepository.save(clientEntity);
      throw new CannotCreateTransactionException("Could not create a client admin user in Keycloak");
    }

    clientEntity.setKeycloakUserProvisioned(true);
    clientRepository.save(clientEntity);
  }

  private void sendWelcomeEmail(ClientEntity clientEntity) {
    clientEntity.setWelcomeEmailAttemptedOn(Instant.now());
    boolean isSent = emailService.sendEmail(new SendEmailDto(clientEntity.getAdminEmail(), "Hello World"));

    if (!isSent) {
      clientRepository.save(clientEntity);
      throw new CannotCreateTransactionException("Could not send welcome email to user");
    }

    clientEntity.setWelcomeEmailSent(true);
    clientRepository.save(clientEntity);
  }

  private void createDatabase(ClientEntity clientEntity) {
    String dbName = String.format("\"%s\"", clientEntity.getUid());
    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM pg_database WHERE datname = ?",
        Integer.class,
        dbName);
    boolean exists = count != null && count > 0;

    if (!exists) {
      jdbcTemplate.execute("CREATE DATABASE " + dbName);
      logger.info("Database {} created.", dbName);
    } else {
      logger.error("Database {} already exists.", dbName);
    }

    tenantDbMigrationService.migrate(clientEntity.getUid());

    clientEntity.setDbProvisionAttemptedOn(Instant.now());
    clientEntity.setDbProvisioned(true);
    clientRepository.save(clientEntity);
  }

  @Recover
  public void recover(Exception ex, String json) {
    logger.error("Final failure after retries for event: {}", json, ex);
  }
}