package com.zyeeda.cdeio.web;

import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class CloseEntityManagerFilter extends EntityManagerControlFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        filterChain.doFilter(request, response);
        
        if (this.hasEntityManagerControlFlag(request)) {
            EntityManagerFactory emf = this.lookupEntityManagerFactory(request);
            EntityManagerHolder emHolder = (EntityManagerHolder)
                    TransactionSynchronizationManager.unbindResource(emf);
            logger.debug("Closing JPA EntityManager in CloseEntityManagerFilter.");
            EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());
        }
        
    }
    
}
