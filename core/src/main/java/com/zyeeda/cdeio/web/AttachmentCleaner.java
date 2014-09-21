package com.zyeeda.cdeio.web;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.zyeeda.cdeio.commons.resource.entity.Attachment;

/**
 * @author guyong
 *
 */
public class AttachmentCleaner {

    private EntityManager entityManager = null;
    private TransactionTemplate transactionTemplate = null;
    //private String path = null;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /*public void setPath(String path) {
        this.path = path;
    }*/

    public void execute() {
        transactionTemplate.execute(new TransactionCallback<Void>() {

            @Override
            public Void doInTransaction(TransactionStatus arg0) {
                TypedQuery<Attachment> q = entityManager.createQuery("from Attachment a where a.draft = true", Attachment.class);
                List<Attachment> attachments = q.getResultList();
                long now = new Date().getTime();
                for (Attachment a : attachments) {
                    long t = now - a.getCreateTime().getTime();
                    if (t > 86400000) {
                        entityManager.remove(a);
                    }
                }
                return null;
            }

        });
    }

}
