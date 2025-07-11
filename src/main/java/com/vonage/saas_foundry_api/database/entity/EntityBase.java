package com.vonage.saas_foundry_api.database.entity;

import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class EntityBase {
  
  @CreationTimestamp
  @Column(name = "created_at")
  private Instant createdAt;
  
  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
