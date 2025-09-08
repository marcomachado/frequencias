package com.petsaudedigital.backend.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import jakarta.persistence.EntityManager;

public class CustomJpaRepository<T, ID> extends SimpleJpaRepository<T, ID> {
    public CustomJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    public CustomJpaRepository(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        try {
            S saved = super.saveAndFlush(entity);
            return saved;
        } catch (RuntimeException ex) {
            if (isUniqueConstraint(ex)) {
                throw new DataIntegrityViolationException(ex.getMessage(), ex);
            }
            throw ex;
        }
    }

    private boolean isUniqueConstraint(Throwable ex) {
        while (ex != null) {
            String msg = ex.getMessage();
            if (msg != null && msg.contains("SQLITE_CONSTRAINT_UNIQUE")) return true;
            ex = ex.getCause();
        }
        return false;
    }
}

