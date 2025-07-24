package com.saas.saas_foundry_api.service.domain;

import java.util.List;
import org.springframework.stereotype.Service;

import com.saas.saas_foundry_api.common.QueueNames;
import com.saas.saas_foundry_api.config.database.TenantQueryRunner;
import com.saas.saas_foundry_api.config.properties.TenantProperties;
import com.saas.saas_foundry_api.database.entity.ClientEntity;
import com.saas.saas_foundry_api.database.entity.UserEntity;
import com.saas.saas_foundry_api.dto.request.ClientDto;
import com.saas.saas_foundry_api.dto.request.UserDto;
import com.saas.saas_foundry_api.dto.response.ClientsDto;
import com.saas.saas_foundry_api.dto.response.UsersDto;
import com.saas.saas_foundry_api.enums.TenantType;
import com.saas.saas_foundry_api.exception.DuplicateResourceException;
import com.saas.saas_foundry_api.exception.ResourceNotFoundException;
import com.saas.saas_foundry_api.mapper.ClientMapper;
import com.saas.saas_foundry_api.mapper.UserMapper;
import com.saas.saas_foundry_api.service.queue.ClientProvisioningEvent;
import com.saas.saas_foundry_api.service.queue.MessageQueue;
import com.saas.saas_foundry_api.service.queue.UserProvisioningEvent;
import com.saas.saas_foundry_api.utils.TenantUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ClientService {

  private final MessageQueue messageQueue;
  private final TenantQueryRunner tenantQueryRunner;
  private final TenantProperties tenantProperties;

  public void createClient(String orgUid, ClientDto clientDto) {
    String tenantName = TenantUtils.getTenantDatabaseName(orgUid, TenantType.ORGANIZATION);

    tenantQueryRunner.runInTenant(tenantName, entityManager -> {
      entityManager.createQuery(
          "FROM ClientEntity c WHERE c.name = :name", ClientEntity.class)
          .setParameter("name", clientDto.getName())
          .setMaxResults(1)
          .getResultStream()
          .findFirst()
          .ifPresent(c -> {
            throw new DuplicateResourceException("Client already added by the organization");
          });

      ClientEntity entity = ClientMapper.toEntity(clientDto);
      entityManager.persist(entity);

      ClientProvisioningEvent event = new ClientProvisioningEvent(entity.getUid(), orgUid);
      messageQueue.sendMessage(QueueNames.CLIENT_PROVISIONING_QUEUE, event);
      return null;
    });
  }

  public ClientsDto listClients(String orgUid, int page, int size) {
    tenantQueryRunner.runInTenant(tenantProperties.getRoot(), entityManager -> {
      boolean exists = entityManager.createQuery(
          "SELECT COUNT(o) FROM OrganizationEntity o WHERE o.uid = :uid", Long.class)
          .setParameter("uid", orgUid)
          .getSingleResult() > 0;

      if (!exists) {
        throw new ResourceNotFoundException("Organization not found");
      }

      return null;
    });

    String tenantName = TenantUtils.getTenantDatabaseName(orgUid, TenantType.ORGANIZATION);

    return tenantQueryRunner.runInTenant(tenantName, entityManager -> {
      List<ClientEntity> clientEntities = entityManager.createQuery(
          "FROM ClientEntity c ORDER BY c.createdAt DESC", ClientEntity.class)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .getResultList();

      long total = entityManager.createQuery(
          "SELECT COUNT(c) FROM ClientEntity c", Long.class)
          .getSingleResult();

      List<ClientDto> dtoList = clientEntities.stream().map(ClientMapper::toDto).toList();
      return new ClientsDto(dtoList, total);
    });
  }

  public void createUser(String clientUid, UserDto userDto) {
    String tenantName = TenantUtils.getTenantDatabaseName(clientUid, TenantType.CLIENT);

    tenantQueryRunner.runInTenant(tenantName, entityManager -> {
      entityManager.createQuery(
          "FROM UserEntity u WHERE u.email = :email", UserEntity.class)
          .setParameter("email", userDto.getEmail())
          .setMaxResults(1)
          .getResultStream()
          .findFirst()
          .ifPresent(user -> {
            throw new DuplicateResourceException("User already exists");
          });

      UserEntity user = UserMapper.toUserEntity(userDto);
      entityManager.persist(user);

      UserProvisioningEvent event = new UserProvisioningEvent(user.getId(), clientUid);
      messageQueue.sendMessage(QueueNames.USER_PROVISIONING_QUEUE, event);
      return null;
    });
  }

  public UsersDto listUsers(String clientUid, int page, int size) {
    String tenantName = TenantUtils.getTenantDatabaseName(clientUid, TenantType.CLIENT);

    return tenantQueryRunner.runInTenant(tenantName, entityManager -> {
      List<UserEntity> userEntities = entityManager
          .createQuery("FROM UserEntity u ORDER BY u.createdAt DESC", UserEntity.class)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .getResultList();

      long total = entityManager.createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class)
          .getSingleResult();

      List<UserDto> dtoList = userEntities.stream().map(UserMapper::toDto).toList();
      return new UsersDto(dtoList, total);
    });
  }
}