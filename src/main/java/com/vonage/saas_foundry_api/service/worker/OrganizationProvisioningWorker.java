package com.vonage.saas_foundry_api.service.worker;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vonage.saas_foundry_api.common.QueueNames;
import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;
import com.vonage.saas_foundry_api.database.repository.OrganizationRepository;
import com.vonage.saas_foundry_api.dto.request.KeycloakUserDto;
import com.vonage.saas_foundry_api.dto.request.SendEmailDto;
import com.vonage.saas_foundry_api.mapper.TenantMapper;
import com.vonage.saas_foundry_api.service.other.EmailService;
import com.vonage.saas_foundry_api.service.other.KeycloakService;
import com.vonage.saas_foundry_api.service.queue.TenantProvisioningEvent;
import com.vonage.saas_foundry_api.utils.OrganizationUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OrganizationProvisioningWorker {

  @Value("${keycloak.application-realm}")
  private String applicationRealm;

  private final OrganizationRepository organizationRepository;
  private final KeycloakService keycloakService;
  private final EmailService emailService;
  private final OrganizationUtils organizationUtils;
  private static final Logger logger = LoggerFactory.getLogger(OrganizationProvisioningWorker.class);

  @RabbitListener(queues = QueueNames.ORGANIZATION_PROVISIONING_QUEUE)
  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
  public void provisionOrganization(String json) throws JsonProcessingException {
    TenantProvisioningEvent event = TenantMapper.toTenantProvisioningEvent(json);
    OrganizationEntity organizationEntity = organizationUtils.findOrgByUid(event.getUid());

    if (!organizationEntity.isKeycloakUserProvisioned()) {
      createKeycloakUser(organizationEntity);
    }

    if (!organizationEntity.isWelcomeEmailSent()) {
      sendWelcomeEmail(organizationEntity);
    }
  }

  private void createKeycloakUser(OrganizationEntity organizationEntity) {
    organizationEntity.setKeycloakUserProvisionAttemptedOn(Instant.now());

    String adminEmail = organizationEntity.getAdminEmail();
    boolean isCreated = keycloakService.createUser(new KeycloakUserDto(adminEmail, adminEmail, applicationRealm));

    if (!isCreated) {
      organizationRepository.save(organizationEntity);
      throw new CannotCreateTransactionException("Could not create a user in Keycloak");
    }

    organizationEntity.setKeycloakUserProvisioned(true);
    organizationRepository.save(organizationEntity);
  }

  private void sendWelcomeEmail(OrganizationEntity organizationEntity) {
    organizationEntity.setWelcomeEmailAttemptedOn(Instant.now());
    boolean isSent = emailService.sendEmail(new SendEmailDto(organizationEntity.getAdminEmail(), "Hello World"));

    if (!isSent) {
      organizationRepository.save(organizationEntity);
      throw new CannotCreateTransactionException("Could not send welcome email to user");
    }

    organizationEntity.setWelcomeEmailSent(true);
    organizationRepository.save(organizationEntity);
  }
  
  @Recover
  public void recover(Exception ex, String json) {
    logger.error("Final failure after retries for event: {}", json, ex);
  }
}