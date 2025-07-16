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
import com.vonage.saas_foundry_api.config.database.DatabaseContextHolder;
import com.vonage.saas_foundry_api.database.entity.UserEntity;
import com.vonage.saas_foundry_api.database.repository.UserRepository;
import com.vonage.saas_foundry_api.dto.request.KeycloakUserDto;
import com.vonage.saas_foundry_api.dto.request.SendEmailDto;
import com.vonage.saas_foundry_api.mapper.UserMapper;
import com.vonage.saas_foundry_api.service.other.EmailService;
import com.vonage.saas_foundry_api.service.other.KeycloakService;
import com.vonage.saas_foundry_api.service.queue.UserProvisioningEvent;
import com.vonage.saas_foundry_api.utils.UserUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserProvisioningWorker {

  private final KeycloakService keycloakService;
  private final EmailService emailService;
  private final UserUtils userUtils;
  private final UserRepository userRepository;
  private static final Logger logger = LoggerFactory.getLogger(UserProvisioningWorker.class);

  @RabbitListener(queues = QueueNames.USER_PROVISIONING_QUEUE)
  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
  public void provisionUser(String json) throws JsonProcessingException {
    UserProvisioningEvent event = UserMapper.toUserProvisioningEvent(json);

    DatabaseContextHolder.setCurrentDb(event.getClientUid());

    UserEntity userEntity = userUtils.findUserById(event.getId());

    if (!userEntity.isKeycloakUserProvisioned()) {
      createKeycloakUser(userEntity, event.getClientUid());
    }

    if (!userEntity.isWelcomeEmailSent()) {
      sendWelcomeEmail(userEntity);
    }
  }

  private void createKeycloakUser(UserEntity userEntity, String clientUid) {
    userEntity.setKeycloakUserProvisionAttemptedOn(Instant.now());

    KeycloakUserDto keycloakUserDto = KeycloakUserDto.builder()
        .username(userEntity.getEmail())
        .email(userEntity.getEmail())
        .firstName(userEntity.getFirstName())
        .lastName(userEntity.getLastName())
        .realm(clientUid)
        .build();
    boolean isCreated = keycloakService.createUser(keycloakUserDto);

    if (!isCreated) {
      userRepository.save(userEntity);
      throw new CannotCreateTransactionException("Could not create a client user in Keycloak");
    }

    userEntity.setKeycloakUserProvisioned(true);
    userRepository.save(userEntity);
  }

  private void sendWelcomeEmail(UserEntity userEntity) {
    userEntity.setWelcomeEmailAttemptedOn(Instant.now());
    boolean isSent = emailService.sendEmail(new SendEmailDto(userEntity.getEmail(), "Hello World"));

    if (!isSent) {
      userRepository.save(userEntity);
      throw new CannotCreateTransactionException("Could not send welcome email to user");
    }

    userEntity.setWelcomeEmailSent(true);
    userRepository.save(userEntity);
  }

  @Recover
  public void recover(Exception ex, String json) {
    logger.error("Final failure after retries for event: {}", json, ex);
  }
}