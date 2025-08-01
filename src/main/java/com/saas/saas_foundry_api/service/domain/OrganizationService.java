package com.saas.saas_foundry_api.service.domain;

import java.util.List;
import org.springframework.stereotype.Service;

import com.saas.saas_foundry_api.common.QueueNames;
import com.saas.saas_foundry_api.config.database.TenantQueryRunner;
import com.saas.saas_foundry_api.config.properties.TenantProperties;
import com.saas.saas_foundry_api.database.entity.OrganizationEntity;
import com.saas.saas_foundry_api.dto.request.OrganizationDto;
import com.saas.saas_foundry_api.dto.response.OrganizationsDto;
import com.saas.saas_foundry_api.exception.DuplicateResourceException;
import com.saas.saas_foundry_api.mapper.OrganizationMapper;
import com.saas.saas_foundry_api.service.queue.MessageQueue;
import com.saas.saas_foundry_api.service.queue.OrganizationProvisioningEvent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OrganizationService {

  private final TenantProperties tenantProperties;
  private final MessageQueue messageQueue;
  private final TenantQueryRunner tenantQueryRunner;

  public void create(OrganizationDto organizationDto) {
    tenantQueryRunner.runInTenant(tenantProperties.getRoot(), entityManager -> {
      // Check for duplicate name
      entityManager.createQuery(
          "FROM OrganizationEntity o WHERE o.name = :name", OrganizationEntity.class)
          .setParameter("name", organizationDto.getName())
          .setMaxResults(1)
          .getResultStream()
          .findFirst()
          .ifPresent(o -> {
            throw new DuplicateResourceException("Organization with the same name already exists");
          });

      // Save new organization
      OrganizationEntity entity = OrganizationMapper.toEntity(organizationDto);
      entityManager.persist(entity);

      // Queue provisioning
      OrganizationProvisioningEvent event = new OrganizationProvisioningEvent(entity.getUid());
      messageQueue.sendMessage(QueueNames.ORGANIZATION_PROVISIONING_QUEUE, event);

      return null;
    });
  }

  public OrganizationsDto list(int page, int size) {
    return tenantQueryRunner.runInTenant(tenantProperties.getRoot(), entityManager -> {
      List<OrganizationEntity> entities = entityManager.createQuery(
          "FROM OrganizationEntity o ORDER BY o.createdAt DESC", OrganizationEntity.class)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .getResultList();

      long total = entityManager.createQuery(
          "SELECT COUNT(o) FROM OrganizationEntity o", Long.class)
          .getSingleResult();

      List<OrganizationDto> dtoList = entities.stream()
          .map(OrganizationMapper::toDto)
          .toList();

      return new OrganizationsDto(dtoList, total);
    });
  }
}
