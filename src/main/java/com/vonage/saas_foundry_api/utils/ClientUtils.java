package com.vonage.saas_foundry_api.utils;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.database.repository.ClientRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ClientUtils {

  private final ClientRepository clientRepository;

  public ClientEntity findClientByUid(String clientUid) {
    Optional<ClientEntity> optionalClientEntity = clientRepository.findById(clientUid);
    if (optionalClientEntity.isEmpty()) {
      throw new NotFoundException("Client not found");
    }

    return optionalClientEntity.get();
  }
}