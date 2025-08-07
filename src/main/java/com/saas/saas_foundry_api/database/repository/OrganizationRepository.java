package com.saas.saas_foundry_api.database.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.saas.saas_foundry_api.database.entity.OrganizationEntity;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {
  Optional<OrganizationEntity> findByName(String name);

  Optional<OrganizationEntity> findByUid(String uid);

  boolean existsByName(String name);
}