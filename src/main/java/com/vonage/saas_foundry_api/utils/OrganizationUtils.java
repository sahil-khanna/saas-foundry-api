package com.vonage.saas_foundry_api.utils;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;
import com.vonage.saas_foundry_api.database.repository.OrganizationRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class OrganizationUtils {

  private final OrganizationRepository organizationRepository;
  
  public OrganizationEntity findOrgByUid(String orgUid) {
    Optional<OrganizationEntity> optionalOrganizationEntity = organizationRepository.findById(orgUid);
    if (optionalOrganizationEntity.isEmpty()) {
      throw new NotFoundException("Organization not found");
    }

    return optionalOrganizationEntity.get();
  }
}
