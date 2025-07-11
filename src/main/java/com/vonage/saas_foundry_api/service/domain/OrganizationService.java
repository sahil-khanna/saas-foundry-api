package com.vonage.saas_foundry_api.service.domain;

import org.springframework.stereotype.Service;
import com.vonage.saas_foundry_api.common.QueueNames;
import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;
import com.vonage.saas_foundry_api.database.repository.OrganizationRepository;
import com.vonage.saas_foundry_api.dto.request.OrganizationDto;
import com.vonage.saas_foundry_api.exception.DuplicateResourceException;
import com.vonage.saas_foundry_api.mapper.OrganizationMapper;
import com.vonage.saas_foundry_api.service.queue.MessageQueue;
import com.vonage.saas_foundry_api.service.queue.TenantProvisioningEvent;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final MessageQueue messageQueue;
  
  public void create(OrganizationDto organizationDto) {
    if (organizationRepository.existsByName(organizationDto.getName())) {
      throw new DuplicateResourceException("Organization with the same name already exists");
    }

    OrganizationEntity organizationEntity = OrganizationMapper.toEntity(organizationDto);
    organizationRepository.save(organizationEntity);

    TenantProvisioningEvent tenantProvisioningEvent = new TenantProvisioningEvent(organizationEntity.getUid());
    messageQueue.sendMessage(QueueNames.ORGANIZATION_PROVISIONING_QUEUE, tenantProvisioningEvent);
  }
}