package com.saas.saas_foundry_api.database.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.saas.saas_foundry_api.database.entity.ClientEntity;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, String> {
  Optional<ClientEntity> findByName(String name);

  Optional<ClientEntity> findByUid(String uid);

  boolean existsByName(String name);
}