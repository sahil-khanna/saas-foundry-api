package com.vonage.saas_foundry_api.service.domain;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.vonage.saas_foundry_api.common.QueueNames;
import com.vonage.saas_foundry_api.config.database.TenantQueryRunner;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;
import com.vonage.saas_foundry_api.database.entity.UserEntity;
import com.vonage.saas_foundry_api.dto.request.ClientDto;
import com.vonage.saas_foundry_api.dto.request.UserDto;
import com.vonage.saas_foundry_api.dto.response.ClientsDto;
import com.vonage.saas_foundry_api.dto.response.UsersDto;
import com.vonage.saas_foundry_api.exception.DuplicateResourceException;
import com.vonage.saas_foundry_api.exception.ResourceNotFoundException;
import com.vonage.saas_foundry_api.mapper.ClientMapper;
import com.vonage.saas_foundry_api.mapper.UserMapper;
import com.vonage.saas_foundry_api.service.queue.MessageQueue;
import com.vonage.saas_foundry_api.service.queue.TenantProvisioningEvent;
import com.vonage.saas_foundry_api.service.queue.UserProvisioningEvent;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ClientService {

  @Value("${tenant.root}")
  private String rootTenant;

  private final MessageQueue messageQueue;
  private final EntityManagerFactory entityManagerFactory;
  private TenantQueryRunner tenantQueryRunner;

  @PostConstruct
  private void init() {
    tenantQueryRunner = new TenantQueryRunner(entityManagerFactory);
  }

  public void createClient(String orgUid, ClientDto clientDto) {
    tenantQueryRunner.runInTenant(rootTenant, entityManager -> {
      // Check for duplicate client
      entityManager.createQuery(
          "FROM ClientEntity c WHERE c.name = :name AND c.organization.uid = :orgUid", ClientEntity.class)
          .setParameter("name", clientDto.getName())
          .setParameter("orgUid", orgUid)
          .setMaxResults(1)
          .getResultStream()
          .findFirst()
          .ifPresent(c -> {
            throw new DuplicateResourceException("Client already added by the organization");
          });

      // Fetch organization
      OrganizationEntity org = entityManager.createQuery(
          "FROM OrganizationEntity o WHERE o.uid = :uid", OrganizationEntity.class)
          .setParameter("uid", orgUid)
          .getResultStream()
          .findFirst()
          .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

      // Save client
      ClientEntity entity = ClientMapper.toEntity(org, clientDto);
      entityManager.persist(entity);

      // Queue provisioning
      TenantProvisioningEvent event = new TenantProvisioningEvent(entity.getUid());
      messageQueue.sendMessage(QueueNames.CLIENT_PROVISIONING_QUEUE, event);
      return null;
    });
  }

  public ClientsDto listClients(String orgUid, int page, int size) {
    return tenantQueryRunner.runInTenant(rootTenant, entityManager -> {
      OrganizationEntity org = entityManager.createQuery(
          "FROM OrganizationEntity o WHERE o.uid = :uid", OrganizationEntity.class)
          .setParameter("uid", orgUid)
          .getResultStream()
          .findFirst()
          .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

      List<ClientEntity> clients = entityManager.createQuery(
          "FROM ClientEntity c WHERE c.organization = :org ORDER BY c.createdAt DESC", ClientEntity.class)
          .setParameter("org", org)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .getResultList();

      long total = entityManager.createQuery(
          "SELECT COUNT(c) FROM ClientEntity c WHERE c.organization = :org", Long.class)
          .setParameter("org", org)
          .getSingleResult();

      List<ClientDto> dtoList = clients.stream().map(ClientMapper::toDto).toList();
      return new ClientsDto(dtoList, total);
    });
  }

  public void createUser(String orgUid, String clientUid, UserDto userDto) {
    tenantQueryRunner.runInTenant(rootTenant, entityManager -> {
      return entityManager.createQuery(
          "FROM ClientEntity c WHERE c.uid = :clientUid AND c.organization.uid = :orgUid", ClientEntity.class)
          .setParameter("clientUid", clientUid)
          .setParameter("orgUid", orgUid)
          .getResultStream()
          .findFirst()
          .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    });

    tenantQueryRunner.runInTenant(clientUid, entityManager -> {
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

  public UsersDto listUsers(String orgUid, String clientUid, int page, int size) {
    tenantQueryRunner.runInTenant(rootTenant, entityManager -> {
      return entityManager.createQuery(
          "FROM ClientEntity c WHERE c.uid = :clientUid AND c.organization.uid = :orgUid", ClientEntity.class)
          .setParameter("clientUid", clientUid)
          .setParameter("orgUid", orgUid)
          .setMaxResults(1)
          .getResultStream()
          .findFirst()
          .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    });

    List<UserEntity> users = tenantQueryRunner.runInTenant(clientUid,
        entityManager -> entityManager.createQuery("FROM UserEntity u ORDER BY u.createdAt DESC", UserEntity.class)
            .setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList());

    long total = tenantQueryRunner.runInTenant(clientUid,
        entityManager -> entityManager.createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class)
            .getSingleResult());

    List<UserDto> dtoList = users.stream().map(UserMapper::toDto).toList();
    return new UsersDto(dtoList, total);
  }
}