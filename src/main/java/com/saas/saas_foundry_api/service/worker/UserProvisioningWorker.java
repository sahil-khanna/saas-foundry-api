package com.saas.saas_foundry_api.service.worker;

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
import com.saas.saas_foundry_api.common.QueueNames;
import com.saas.saas_foundry_api.config.database.TenantRepositoryExecutor;
import com.saas.saas_foundry_api.database.entity.UserEntity;
import com.saas.saas_foundry_api.database.repository.UserRepository;
import com.saas.saas_foundry_api.dto.request.KeycloakUserDto;
import com.saas.saas_foundry_api.dto.request.SendEmailDto;
import com.saas.saas_foundry_api.enums.TenantType;
import com.saas.saas_foundry_api.mapper.UserMapper;
import com.saas.saas_foundry_api.service.other.EmailService;
import com.saas.saas_foundry_api.service.other.KeycloakService;
import com.saas.saas_foundry_api.service.queue.UserProvisioningEvent;
import com.saas.saas_foundry_api.utils.TenantUtils;
import com.saas.saas_foundry_api.utils.UserUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserProvisioningWorker {

  private final KeycloakService keycloakService;
  private final EmailService emailService;
  private final UserUtils userUtils;
  private final TenantRepositoryExecutor tenantRepositoryExecutor;
  private static final Logger logger = LoggerFactory.getLogger(UserProvisioningWorker.class);

  @RabbitListener(queues = QueueNames.USER_PROVISIONING_QUEUE)
  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
  public void provisionUser(String json) throws JsonProcessingException {
    UserProvisioningEvent event = UserMapper.toUserProvisioningEvent(json);

    String clientTenantName = TenantUtils.getTenantDatabaseName(event.getClientUid(), TenantType.CLIENT);

    UserEntity userEntity = userUtils.findUserById(clientTenantName, event.getId());

    if (!userEntity.isKeycloakUserProvisioned()) {
      createKeycloakUser(clientTenantName, userEntity, event.getClientUid());
    }

    if (!userEntity.isWelcomeEmailSent()) {
      sendWelcomeEmail(clientTenantName, userEntity);
    }
  }

  private void createKeycloakUser(String clientTenantName, UserEntity userEntity, String clientUid) {
    KeycloakUserDto keycloakUserDto = KeycloakUserDto.builder()
        .username(userEntity.getEmail())
        .email(userEntity.getEmail())
        .firstName(userEntity.getFirstName())
        .lastName(userEntity.getLastName())
        .realm(clientUid)
        .build();
    boolean isCreated = keycloakService.createUser(keycloakUserDto);
    userEntity.setKeycloakUserProvisioned(isCreated);
    userEntity.setKeycloakUserProvisionAttemptedOn(Instant.now());

    updateUserEntity(clientTenantName, userEntity);

    if (!isCreated) {
      throw new CannotCreateTransactionException("Could not create a client user in Keycloak");
    }
  }

  private void sendWelcomeEmail(String clientTenantName, UserEntity userEntity) {
    boolean isSent = emailService.sendEmail(new SendEmailDto(userEntity.getEmail(), "Hello World"));
    userEntity.setWelcomeEmailSent(isSent);
    userEntity.setWelcomeEmailAttemptedOn(Instant.now());

    updateUserEntity(clientTenantName, userEntity);

    if (!isSent) {
      throw new CannotCreateTransactionException("Could not send welcome email to user");
    }
  }

  private void updateUserEntity(String clientTenantName, UserEntity userEntity) {
    tenantRepositoryExecutor.execute(clientTenantName, UserRepository.class, repository -> {
      repository.save(userEntity);
      return null;
    });
  }

  @Recover
  public void recover(Exception ex, String json) {
    logger.error("Final failure after retries for event: {}", json, ex);
  }
}