package com.saas.saas_foundry_api.config.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.stereotype.Component;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class TenantRepositoryExecutor {

  private final EntityManagerFactory entityManagerFactory;

  public <R, T> T runInTenant(String tenantId, Class<R> repoClass, Function<R, T> logic) {
    String originalTenant = TenantContext.getTenantId();

    try {
      TenantContext.setTenantId(tenantId);

      EntityManager em = entityManagerFactory.createEntityManager();
      try {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        JpaRepositoryFactory factory = new JpaRepositoryFactory(em);
        R repo = factory.getRepository(repoClass);

        T result = logic.apply(repo);
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
