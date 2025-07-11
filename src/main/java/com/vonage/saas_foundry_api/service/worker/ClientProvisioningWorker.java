package com.vonage.saas_foundry_api.service.worker;

import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vonage.saas_foundry_api.common.QueueNames;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.database.repository.ClientRepository;
import com.vonage.saas_foundry_api.dto.request.KeycloakRealmDto;
import com.vonage.saas_foundry_api.dto.request.KeycloakUserDto;
import com.vonage.saas_foundry_api.dto.request.SendEmailDto;
import com.vonage.saas_foundry_api.mapper.TenantMapper;
import com.vonage.saas_foundry_api.service.other.EmailService;
import com.vonage.saas_foundry_api.service.other.KeycloakService;
import com.vonage.saas_foundry_api.service.queue.TenantProvisioningEvent;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ClientProvisioningWorker {

  private final ClientRepository clientRepository;
  private final KeycloakService keycloakService;
  private final EmailService emailService;
  private final JdbcTemplate jdbcTemplate;
  private static final Logger logger = LoggerFactory.getLogger(ClientProvisioningWorker.class);

  @RabbitListener(queues = QueueNames.CLIENT_PROVISIONING_QUEUE)
  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
  public void provisionClient(String json) throws JsonProcessingException {
    TenantProvisioningEvent event = TenantMapper.toTenantProvisioningEvent(json);
    String uid = event.getUid();

    Optional<ClientEntity> optionalClientEntity = clientRepository.findById(uid);
    if (optionalClientEntity.isEmpty()) {
      throw new NotFoundException();
    }

    ClientEntity clientEntity = optionalClientEntity.get();

    if (!clientEntity.isKeycloakRealmProvisioned()) {
      createKeycloakRealm(clientEntity);
    }

    if (!clientEntity.isKeycloakUserProvisioned()) {
      createKeycloakUser(clientEntity);
    }

    if (!clientEntity.isDbProvisioned()) {
      createDatabase(clientEntity);
    }

    if (!clientEntity.isWelcomeEmailSent()) {
      sendWelcomeEmail(clientEntity);
    }
  }

  private void createKeycloakRealm(ClientEntity clientEntity) {
    clientEntity.setKeycloakRealmProvisionAttemptedOn(Instant.now());

    boolean isCreated = keycloakService.createRealm(new KeycloakRealmDto(clientEntity.getUid(), clientEntity.getName()));

    if (!isCreated) {
      clientRepository.save(clientEntity);
      throw new CannotCreateTransactionException("Could not create a Keycloak Realm");
    }

    clientEntity.setKeycloakRealmProvisioned(true);
    clientRepository.save(clientEntity);
  }

  private void createKeycloakUser(ClientEntity clientEntity) {
    clientEntity.setKeycloakUserProvisionAttemptedOn(Instant.now());

    String adminEmail = clientEntity.getAdminEmail();
    boolean isCreated = keycloakService.createUser(new KeycloakUserDto(adminEmail, adminEmail, clientEntity.getUid()));

    if (!isCreated) {
      clientRepository.save(clientEntity);
      throw new CannotCreateTransactionException("Could not create a user in Keycloak");
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

    clientEntity.setDbProvisionAttemptedOn(Instant.now());
    clientEntity.setDbProvisioned(true);
    clientRepository.save(clientEntity);
  }
}