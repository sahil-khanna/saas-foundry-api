package com.saas.saas_foundry_api.service.domain;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import com.saas.saas_foundry_api.common.QueueNames;
import com.saas.saas_foundry_api.config.database.TenantRepositoryExecutor;
import com.saas.saas_foundry_api.config.properties.TenantProperties;
import com.saas.saas_foundry_api.database.entity.OrganizationEntity;
import com.saas.saas_foundry_api.database.repository.OrganizationRepository;
import com.saas.saas_foundry_api.dto.request.OrganizationDto;
import com.saas.saas_foundry_api.dto.response.OrganizationsDto;
import com.saas.saas_foundry_api.exception.DuplicateResourceException;
import com.saas.saas_foundry_api.mapper.OrganizationMapper;
import com.saas.saas_foundry_api.service.queue.MessageQueue;
import com.saas.saas_foundry_api.service.queue.TenantProvisioningEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OrganizationService {

  private final MessageQueue messageQueue;
  private final TenantRepositoryExecutor tenantRepositoryExecutor;
  private final TenantProperties tenantProperties;

  public void create(OrganizationDto organizationDto) {
    boolean organizationExists = tenantRepositoryExecutor.runInTenant(
        tenantProperties.getRoot(),
        OrganizationRepository.class,
        repository -> repository.existsByName(organizationDto.getName()));

    if (organizationExists) {
      throw new DuplicateResourceException("Organization with the same name already exists");
    }

    OrganizationEntity organizationEntity = OrganizationMapper.toEntity(organizationDto);
    tenantRepositoryExecutor.runInTenant(
        tenantProperties.getRoot(),
        OrganizationRepository.class,
        repository -> {
          repository.save(organizationEntity);
          return null;
        });

    TenantProvisioningEvent event = new TenantProvisioningEvent(organizationEntity.getUid());
    messageQueue.sendMessage(QueueNames.ORGANIZATION_PROVISIONING_QUEUE, event);
  }

  public OrganizationsDto list(int page, int size) {
    Page<OrganizationEntity> organizationsPage = tenantRepositoryExecutor.runInTenant(
        tenantProperties.getRoot(),
        OrganizationRepository.class,
        repository -> {
          Sort sort = Sort.by(Direction.ASC, "createdAt");
          Pageable pageable = PageRequest.of(page, size, sort);
          return repository.findAll(pageable);
        });

    List<OrganizationDto> organizations = organizationsPage.stream().map(OrganizationMapper::toDto).toList();
    return new OrganizationsDto(organizations, organizationsPage.getTotalElements());
  }
}
