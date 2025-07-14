package com.vonage.saas_foundry_api.utils;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.vonage.saas_foundry_api.database.entity.UserEntity;
import com.vonage.saas_foundry_api.database.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserUtils {

  private final UserRepository userRepository;

  public UserEntity findUserById(long id) {
    Optional<UserEntity> optionalUserEntity = userRepository.findById(id);
    if (optionalUserEntity.isEmpty()) {
      throw new NotFoundException("User not found");
    }

    return optionalUserEntity.get();
  }
}