package com.vonage.saas_foundry_api.service.domain;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.vonage.saas_foundry_api.common.QueueNames;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;
import com.vonage.saas_foundry_api.database.repository.ClientRepository;
import com.vonage.saas_foundry_api.database.repository.OrganizationRepository;
import com.vonage.saas_foundry_api.dto.request.ClientDto;
import com.vonage.saas_foundry_api.exception.DuplicateResourceException;
import com.vonage.saas_foundry_api.mapper.ClientMapper;
import com.vonage.saas_foundry_api.service.queue.MessageQueue;
import com.vonage.saas_foundry_api.service.queue.TenantProvisioningEvent;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ClientService {

  private final OrganizationRepository organizationRepository;
  private final ClientRepository clientRepository;
  private final MessageQueue messageQueue;

  public void create(String orgUid, ClientDto clientDto) {
    Optional<OrganizationEntity> optionalOrganizationEntity = organizationRepository.findById(orgUid);
    if (optionalOrganizationEntity.isEmpty()) {
      throw new NotFoundException("Organization not found");
    }
    
    OrganizationEntity organizationEntity = optionalOrganizationEntity.get();

    if (clientRepository.existsByOrganizationAndName(organizationEntity, clientDto.getName())) {
      throw new DuplicateResourceException("Client already added by the organization");
    }

    ClientEntity clientEntity = ClientMapper.toEntity(organizationEntity, clientDto);
    clientRepository.save(clientEntity);

    TenantProvisioningEvent tenantProvisioningEvent = new TenantProvisioningEvent(clientEntity.getUid());
    messageQueue.sendMessage(QueueNames.CLIENT_PROVISIONING_QUEUE, tenantProvisioningEvent);
  }
}