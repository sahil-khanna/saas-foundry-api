package com.vonage.saas_foundry_api.config.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import lombok.RequiredArgsConstructor;
import java.util.function.Function;

@RequiredArgsConstructor
public class TenantQueryRunner {

  private final EntityManagerFactory entityManagerFactory;

  public <T> T runInTenant(String tenantId, Function<EntityManager, T> logic) {
    String originalTenant = TenantContext.getTenantId();

    try {
      TenantContext.setTenantId(tenantId);

      EntityManager em = entityManagerFactory.createEntityManager();
      try {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        T result = logic.apply(em);
        tx.commit();
        return result;
      } catch (RuntimeException e) {
        em.getTransaction().rollback();
        throw e;
      } finally {
        em.close();
      }
    } finally {
      TenantContext.setTenantId(originalTenant);
    }
  }
}
