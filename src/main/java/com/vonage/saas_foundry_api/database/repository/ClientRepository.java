package com.vonage.saas_foundry_api.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;

public interface ClientRepository extends JpaRepository<ClientEntity, String> {
  boolean existsByOrganizationAndName(OrganizationEntity organization, String name);
}
