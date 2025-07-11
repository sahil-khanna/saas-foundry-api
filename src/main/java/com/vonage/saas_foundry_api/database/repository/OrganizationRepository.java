package com.vonage.saas_foundry_api.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String>  {
  boolean existsByName(String name);
}
