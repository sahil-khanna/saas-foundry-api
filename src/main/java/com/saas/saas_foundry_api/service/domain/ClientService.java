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
import com.saas.saas_foundry_api.database.entity.ClientEntity;
import com.saas.saas_foundry_api.database.entity.UserEntity;
import com.saas.saas_foundry_api.database.repository.ClientRepository;
import com.saas.saas_foundry_api.database.repository.UserRepository;
import com.saas.saas_foundry_api.dto.request.ClientDto;
import com.saas.saas_foundry_api.dto.request.UserDto;
import com.saas.saas_foundry_api.dto.response.ClientsDto;
import com.saas.saas_foundry_api.dto.response.UsersDto;
import com.saas.saas_foundry_api.enums.TenantType;
import com.saas.saas_foundry_api.exception.DuplicateResourceException;
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
  private final TenantRepositoryExecutor tenantRepositoryExecutor;

  public void createClient(String orgUid, ClientDto clientDto) {
    String tenantName = TenantUtils.getTenantDatabaseName(orgUid, TenantType.ORGANIZATION);

    boolean clientExists = tenantRepositoryExecutor.runInTenant(
        tenantName,
        ClientRepository.class,
        repository -> repository.existsByName(clientDto.getName()));

    if (clientExists) {
      throw new DuplicateResourceException("Client with the same name already exists");
    }

    ClientEntity clientEntity = ClientMapper.toEntity(clientDto);
    tenantRepositoryExecutor.runInTenant(
        tenantName,
        ClientRepository.class,
        repository -> {
          repository.save(clientEntity);
          return null;
        });

    ClientProvisioningEvent event = new ClientProvisioningEvent(clientEntity.getUid(), orgUid);
    messageQueue.sendMessage(QueueNames.CLIENT_PROVISIONING_QUEUE, event);
  }

  public ClientsDto listClients(String orgUid, int page, int size) {
    String tenantName = TenantUtils.getTenantDatabaseName(orgUid, TenantType.ORGANIZATION);

    Page<ClientEntity> clientsPage = tenantRepositoryExecutor.runInTenant(
        tenantName,
        ClientRepository.class,
        repository -> repository.findAll(PageRequest.of(page, size)));

    List<ClientDto> clients = clientsPage.stream().map(ClientMapper::toDto).toList();
    return new ClientsDto(clients, clientsPage.getTotalElements());
  }

  public void createUser(String clientUid, UserDto userDto) {
    String tenantName = TenantUtils.getTenantDatabaseName(clientUid, TenantType.CLIENT);

    boolean userExists = tenantRepositoryExecutor.runInTenant(
        tenantName,
        UserRepository.class,
        repository -> repository.existsByEmail(userDto.getEmail()));

    if (userExists) {
      throw new DuplicateResourceException("User with the same email already exists");
    }

    UserEntity userEntity = UserMapper.toEntity(userDto);
    tenantRepositoryExecutor.runInTenant(
        tenantName,
        UserRepository.class,
        repository -> {
          repository.save(userEntity);
          return null;
        });

    UserProvisioningEvent event = new UserProvisioningEvent(userEntity.getId(), clientUid);
    messageQueue.sendMessage(QueueNames.USER_PROVISIONING_QUEUE, event);
  }

  public UsersDto listUsers(String clientUid, int page, int size) {
    String tenantName = TenantUtils.getTenantDatabaseName(clientUid, TenantType.CLIENT);

    Page<UserEntity> usersPage = tenantRepositoryExecutor.runInTenant(
        tenantName,
        UserRepository.class,
        repository -> {
          Sort sort = Sort.by(Direction.ASC, "createdAt");
          Pageable pageable = PageRequest.of(page, size, sort);
          return repository.findAll(pageable);
        });

    List<UserDto> users = usersPage.stream().map(UserMapper::toDto).toList();
    return new UsersDto(users, usersPage.getTotalElements());
  }
}