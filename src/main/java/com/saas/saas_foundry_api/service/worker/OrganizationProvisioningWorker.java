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
import com.saas.saas_foundry_api.config.properties.KeycloakProperties;
import com.saas.saas_foundry_api.config.properties.TenantProperties;
import com.saas.saas_foundry_api.database.entity.OrganizationEntity;
import com.saas.saas_foundry_api.database.repository.OrganizationRepository;
import com.saas.saas_foundry_api.dto.request.KeycloakUserDto;
import com.saas.saas_foundry_api.dto.request.SendEmailDto;
import com.saas.saas_foundry_api.enums.TenantType;
import com.saas.saas_foundry_api.mapper.OrganizationMapper;
import com.saas.saas_foundry_api.service.other.DatabaseService;
import com.saas.saas_foundry_api.service.other.EmailService;
import com.saas.saas_foundry_api.service.other.KeycloakService;
import com.saas.saas_foundry_api.service.other.TenantDbMigrationService;
import com.saas.saas_foundry_api.service.queue.TenantProvisioningEvent;
import com.saas.saas_foundry_api.utils.OrganizationUtils;
import com.saas.saas_foundry_api.utils.TenantUtils;
import com.saas.saas_foundry_api.utils.ThreadUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OrganizationProvisioningWorker {

  private final KeycloakProperties keycloakProperties;
  private final TenantProperties tenantProperties;
  private final TenantRepositoryExecutor tenantRepositoryExecutor;
  private final KeycloakService keycloakService;
  private final EmailService emailService;
  private final OrganizationUtils organizationUtils;
  private final DatabaseService databaseService;
  private final TenantDbMigrationService tenantDbMigrationService;
  private static final Logger logger = LoggerFactory.getLogger(OrganizationProvisioningWorker.class);

  @RabbitListener(queues = QueueNames.ORGANIZATION_PROVISIONING_QUEUE)
  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
  public void provisionOrganization(String json) throws JsonProcessingException {
    TenantProvisioningEvent event = OrganizationMapper.toProvisioningEvent(json);
    OrganizationEntity organizationEntity = organizationUtils.findOrgByUid(event.getUid());

    if (!organizationEntity.isKeycloakUserProvisioned()) {
      createKeycloakUser(organizationEntity);
    }

    if (!organizationEntity.isDbProvisioned()) {
      createDatabase(organizationEntity);
    }

    if (!organizationEntity.isWelcomeEmailSent()) {
      sendWelcomeEmail(organizationEntity);
    }
  }

  private void createKeycloakUser(OrganizationEntity organizationEntity) {
    KeycloakUserDto keycloakUserDto = KeycloakUserDto.builder()
        .username(organizationEntity.getAdminEmail())
        .email(organizationEntity.getAdminEmail())
        .firstName("Admin")
        .lastName("Admin")
        .realm(keycloakProperties.getOrganizationRealm())
        .build();

    boolean isCreated = keycloakService.createUser(keycloakUserDto);
    organizationEntity.setKeycloakUserProvisioned(isCreated);
    organizationEntity.setKeycloakUserProvisionAttemptedOn(Instant.now());

    updateOrganizationEntity(organizationEntity);

    if (!isCreated) {
      throw new CannotCreateTransactionException("Could not create a organization admin user in Keycloak");
    }
  }

  private void sendWelcomeEmail(OrganizationEntity organizationEntity) {
    boolean isSent = emailService.sendEmail(new SendEmailDto(organizationEntity.getAdminEmail(), "Hello World"));
    organizationEntity.setWelcomeEmailSent(isSent);
    organizationEntity.setWelcomeEmailAttemptedOn(Instant.now());

    updateOrganizationEntity(organizationEntity);

    if (!isSent) {
      throw new CannotCreateTransactionException("Could not send welcome email to user");
    }
  }

  private void createDatabase(OrganizationEntity organizationEntity) {
    String dbName = TenantUtils.getTenantDatabaseName(organizationEntity.getUid(), TenantType.ORGANIZATION);
    databaseService.createDatabase(dbName);
    organizationEntity.setDbProvisioned(true);
    organizationEntity.setDbProvisionAttemptedOn(Instant.now());

    // ThreadUtils.sleep(10000, "Sleeping for 5 seconds before migrating schema to the new database.");
    
    tenantDbMigrationService.migrate(dbName, TenantType.ORGANIZATION);

    updateOrganizationEntity(organizationEntity);
  }

  private void updateOrganizationEntity(OrganizationEntity organizationEntity) {
    tenantRepositoryExecutor.runInTenant(tenantProperties.getRoot(), OrganizationRepository.class, repository -> {
      repository.save(organizationEntity);
      return null;
    });
  }

  @Recover
  public void recover(Exception ex, String json) {
    logger.error("Final failure after retries for event: {}", json, ex);
  }
}