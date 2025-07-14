package com.vonage.saas_foundry_api.service.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.vonage.saas_foundry_api.common.QueueNames;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;
import com.vonage.saas_foundry_api.database.entity.UserEntity;
import com.vonage.saas_foundry_api.database.repository.ClientRepository;
import com.vonage.saas_foundry_api.database.repository.UserRepository;
import com.vonage.saas_foundry_api.dto.request.ClientDto;
import com.vonage.saas_foundry_api.dto.request.UserDto;
import com.vonage.saas_foundry_api.dto.response.ClientsDto;
import com.vonage.saas_foundry_api.dto.response.UsersDto;
import com.vonage.saas_foundry_api.exception.DuplicateResourceException;
import com.vonage.saas_foundry_api.mapper.ClientMapper;
import com.vonage.saas_foundry_api.mapper.UserMapper;
import com.vonage.saas_foundry_api.service.queue.MessageQueue;
import com.vonage.saas_foundry_api.service.queue.TenantProvisioningEvent;
import com.vonage.saas_foundry_api.service.queue.UserProvisioningEvent;
import lombok.AllArgsConstructor;
import com.vonage.saas_foundry_api.utils.OrganizationUtils;
import jakarta.ws.rs.NotFoundException;

@AllArgsConstructor
@Service
public class ClientService {

  private final ClientRepository clientRepository;
  private final UserRepository userRepository;
  private final MessageQueue messageQueue;
  private final OrganizationUtils organizationUtils;

  public void createUser(String orgUid, ClientDto clientDto) {
    if (clientRepository.existsByNameAndOrganization_Uid(clientDto.getName(), orgUid)) {
      throw new DuplicateResourceException("Client already added by the organization");
    }

    OrganizationEntity organizationEntity = organizationUtils.findOrgByUid(orgUid);
    ClientEntity clientEntity = ClientMapper.toEntity(organizationEntity, clientDto);
    clientRepository.save(clientEntity);

    TenantProvisioningEvent tenantProvisioningEvent = new TenantProvisioningEvent(clientEntity.getUid());
    messageQueue.sendMessage(QueueNames.CLIENT_PROVISIONING_QUEUE, tenantProvisioningEvent);
  }
  
  public ClientsDto listClients(String orgUid, int page, int size) {
    OrganizationEntity organizationEntity = organizationUtils.findOrgByUid(orgUid);

    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<ClientEntity> clientsPage = clientRepository.findAllByOrganization(organizationEntity, pageable);
    List<ClientDto> dtoList = clientsPage.stream().map(ClientMapper::toDto).toList();

    return new ClientsDto(dtoList, clientsPage.getTotalElements());
  }

  public void createUser(String orgUid, String clientUid, UserDto userDto) {
    Optional<ClientEntity> optionalClientEntity = clientRepository.findByUidAndOrganization_Uid(clientUid, orgUid);
    if (optionalClientEntity.isEmpty()) {
      throw new NotFoundException("Client not found");
    }

    if (userRepository.existsByEmailAndClient_Uid(userDto.getEmail(), clientUid)) {
      throw new DuplicateResourceException("User already exists");
    }

    UserEntity userEntity = UserMapper.toUserEntity(optionalClientEntity.get(), userDto);
    userRepository.save(userEntity);

    UserProvisioningEvent userProvisioningEvent = new UserProvisioningEvent(userEntity.getId());
    messageQueue.sendMessage(QueueNames.USER_PROVISIONING_QUEUE, userProvisioningEvent);
  }
  
  public UsersDto listUsers(String orgUid, String clientUid, int page, int size) {
    Optional<ClientEntity> optionalClientEntity = clientRepository.findByUidAndOrganization_Uid(clientUid, orgUid);
    if (optionalClientEntity.isEmpty()) {
      throw new NotFoundException("Client not found");
    }

    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<UserEntity> usersPage = userRepository.findAllByClient(optionalClientEntity.get(), pageable);
    List<UserDto> dtoList = usersPage.stream().map(UserMapper::toDto).toList();

    return new UsersDto(dtoList, usersPage.getTotalElements());
  }
}