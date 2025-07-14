package com.vonage.saas_foundry_api.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vonage.saas_foundry_api.database.entity.ClientEntity;
import com.vonage.saas_foundry_api.database.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  boolean existsByEmailAndClient_Uid(String email, String clientUid);

  Page<UserEntity> findAllByClient(ClientEntity clientEntity, Pageable pageable);
}
