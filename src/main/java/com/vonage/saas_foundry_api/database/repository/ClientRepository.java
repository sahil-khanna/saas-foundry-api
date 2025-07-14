package com.vonage.saas_foundry_api.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.database.entity.OrganizationEntity;
import java.util.List;
import java.util.Optional;


public interface ClientRepository extends JpaRepository<ClientEntity, String> {
  boolean existsByNameAndOrganization_Uid(String name, String orgUid);

  Page<ClientEntity> findAllByOrganization(OrganizationEntity organizationEntity, Pageable pageable);
  
  Optional<ClientEntity> findByUidAndOrganization_Uid(String uid, String orgUid);
}
