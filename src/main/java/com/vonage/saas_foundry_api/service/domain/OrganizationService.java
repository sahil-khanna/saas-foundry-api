package com.vonage.saas_foundry_api.service.domain;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.vonage.saas_foundry_api.common.QueueNames;
import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;
import com.vonage.saas_foundry_api.database.repository.OrganizationRepository;
import com.vonage.saas_foundry_api.dto.request.OrganizationDto;
import com.vonage.saas_foundry_api.dto.response.OrganizationsDto;
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

  public OrganizationsDto list(int page, int size) {
    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<OrganizationEntity> organizationsPage = organizationRepository.findAll(pageable);
    List<OrganizationDto> dtoList = organizationsPage.stream().map(OrganizationMapper::toDto).toList();

    return new OrganizationsDto(dtoList, organizationsPage.getTotalElements());
  }
}