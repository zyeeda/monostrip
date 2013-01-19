package com.zyeeda.coala.web;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class OpenEntityManagerFilter extends EntityManagerControlFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        EntityManagerFactory emf = this.lookupEntityManagerFactory(request);
        
        if (!TransactionSynchronizationManager.hasResource(emf)) {
            logger.debug("Opening JPA EntityManager in OpenEntityManagerFilter.");
            try {
                EntityManager em = this.createEntityManager(emf);
                TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em));
                this.setEntityManagerControlFlag(request);
            } catch (PersistenceException ex) {
                throw new DataAccessResourceFailureException("Could not create JPA EntityManager", ex);
            }
        }
        
        filterChain.doFilter(request, response);
    }

}
