/**
 * 
 */
package com.zyeeda.coala.commons.manager.support;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;

import com.zyeeda.coala.commons.manager.GenericManager;

/**
 * @author guyong
 *
 */
public class DefaultGenericManager<T, ID extends Serializable> implements GenericManager<T, ID> {
    
    private Class<T> entityClass = null;
    private EntityManager entityManager = null;
    
    public DefaultGenericManager() {
        entityClass = getObjectClass();
    }
    
    @Override
    public T get(ID id) {
        return getEntityManager().find(entityClass, id);
    }

    @Override
    public List<T> get(ID... ids) {
        List<T> result = new ArrayList<T>();
        for( ID id : ids ) {
            result.add(get(id));
        }
        return result;
    }

    @Override
    public List<T> get(Collection<ID> ids) {
        List<T> result = new ArrayList<T>();
        for( ID id : ids ) {
            result.add(get(id));
        }
        return result;
    }

    @Override
    public T getReference(ID id) {
        return getEntityManager().getReference(entityClass, id);
    }

    @Override
    public List<T> getReference(ID... ids) {
        List<T> result = new ArrayList<T>();
        for( ID id : ids ) {
            result.add(getReference(id));
        }
        return result;
    }

    @Override
    public List<T> getReference(Collection<ID> ids) {
        List<T> result = new ArrayList<T>();
        for( ID id : ids ) {
            result.add(getReference(id));
        }
        return result;
    }

    @Override
    public T save(T entity) {
        getEntityManager().persist(entity);
        return entity;
    }

    @Override
    public List<T> save(T... entities) {
        List<T> result = new ArrayList<T>();
        for( T entity : entities ) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public List<T> save(Collection<T> entities) {
        List<T> result = new ArrayList<T>();
        for( T entity : entities ) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public T update(T entity) {
        return getEntityManager().merge(entity);
    }

    @Override
    public List<T> update(T... entities) {
        List<T> result = new ArrayList<T>();
        for( T entity : entities ) {
            result.add(update(entity));
        }
        return result;
    }

    @Override
    public List<T> update(Collection<T> entities) {
        List<T> result = new ArrayList<T>();
        for( T entity : entities ) {
            result.add(update(entity));
        }
        return result;
    }

    @Override
    public T remove(T entity) {
        getEntityManager().remove(entity);
        return entity;
    }

    @Override
    public List<T> remove(T... entities) {
        List<T> result = new ArrayList<T>();
        for( T entity : entities ) {
            result.add(remove(entity));
        }
        return result;
    }

    @Override
    public List<T> remove(Collection<T> entities) {
        List<T> result = new ArrayList<T>();
        for( T entity : entities ) {
            result.add(remove(entity));
        }
        return result;
    }

    @Override
    public T removeById(ID id) {
        T entity = getReference(id);
        return remove(entity);
    }

    @Override
    public List<T> removeById(ID... ids) {
        List<T> result = new ArrayList<T>();
        for( ID id : ids ) {
            result.add(removeById(id));
        }
        return result;
    }

    @Override
    public List<T> removeById(Collection<ID> ids) {
        List<T> result = new ArrayList<T>();
        for( ID id : ids ) {
            result.add(removeById(id));
        }
        return result;
    }

    @Override
    public List<T> list(Order... orders) {
        return createGetAllQuery(orders).getResultList();
    }

    @Override
    public List<T> list(int pageSize, int pageNum, Order... orders) {
        TypedQuery<T> query = createGetAllQuery(orders);
        
        int firstResult = pageNum < 1 ? 0 : pageNum * pageSize;
        query.setFirstResult(firstResult);
        query.setMaxResults(pageSize);
        
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findByExample(T entity, Order... orders) {
        return createByExampleCriteria(entity, orders).list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findByExample(T entity, int pageSize, int pageNum, Order... orders) {
        Criteria criteria = createByExampleCriteria(entity, orders);
        
        int firstResult = pageNum < 1 ? 0 : pageNum * pageSize;
        criteria.setFirstResult(firstResult);
        criteria.setMaxResults(pageSize);
        
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findByNamedQuery(String name, Map<String, Object> parameters) {
        return findByNamedQueryWithNoGeneric(name, parameters);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findByNamedQuery(String name, Map<String, Object> parameters, int pageSize, int pageNum) {
        return findByNamedQueryWithNoGeneric(name, parameters, pageSize, pageNum);
    }

    @Override
    public List<?> findUntypedByNamedQuery(String name, Map<String, Object> parameters) {
        return findByNamedQueryWithNoGeneric(name, parameters);
    }

    @Override
    public List<?> findUntypedByNamedQuery(String name, Map<String, Object> parameters, int pageSize, int pageNum) {
        return findByNamedQueryWithNoGeneric(name, parameters, pageSize, pageNum);
    }

    @Override
    public T findSingleResultByExample(T entity, Order... orders) {
        List<T> results = findByExample(entity, orders);
        if( results.isEmpty() ) {
            throw new NoResultException("No entity found for example " + entity);
        }
        if( results.size() > 1 ) {
            throw new NonUniqueResultException( "result returns more than one elements" );
        }
        return results.get(0);
    }

    @Override
    public T findSingleResultByExample(T entity, int pageSize, int pageNum, Order... orders) {
        List<T> results = findByExample(entity, pageSize, pageNum, orders);
        if( results.isEmpty() ) {
            throw new NoResultException("No entity found for example " + entity);
        }
        if( results.size() > 1 ) {
            throw new NonUniqueResultException( "result returns more than one elements" );
        }
        return results.get(0);
    }

    @Override
    public Object findSingleResultByNamedQuery(String name, Map<String, Object> parameters) {
        return createNamedQuery(name, parameters).getSingleResult();
    }

    @Override
    public Object findSingleResultByNamedQuery(String name, Map<String, Object> parameters, int pageSize, int pageNum) {
        Query query = createNamedQuery(name, parameters);
        
        int firstResult = pageNum < 1 ? 0 : pageNum * pageSize;
        query.setFirstResult(firstResult);
        query.setMaxResults(pageSize);
        
        return query.getSingleResult();
    }

    @Override
    public int executeNamedQuery(String name, Map<String, Object> parameters) {
        return createNamedQuery(name, parameters).executeUpdate();
    }

    @Override
    public void flush() {
        getEntityManager().flush();
    }

    @Override
    public T refresh(T entity) {
        getEntityManager().refresh(entity);
        return entity;
    }

    @Override
    public List<T> refresh(T... entities) {
        List<T> result = new ArrayList<T>();
        for( T entity : entities ) {
            result.add(refresh(entity));
        }
        return result;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    private Class<T> getObjectClass(){
        Type type = this.getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType)type;
        Type[] types = pt.getActualTypeArguments();
        return (Class<T>)types[0];
    }
    
    private TypedQuery<T> createGetAllQuery(Order... orders) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        
        List<javax.persistence.criteria.Order> os = new ArrayList<javax.persistence.criteria.Order>();
        for( Order o : orders ) {
            os.add(o.isDesc() ? cb.desc(root.get(o.getProperty())) : cb.asc(root.get(o.getProperty())));
        }
        cq.orderBy(os);
        return getEntityManager().createQuery(cq);
    }
    
    private Criteria createByExampleCriteria(T entity, Order... orders) {
        Example example = Example.create(entity).excludeZeroes();
        Criteria criteria = ((Session)getEntityManager().getDelegate()).createCriteria(entityClass);
        criteria.add(example);

        for( Order o : orders ) {
            criteria.addOrder( o.isDesc() ? 
                    org.hibernate.criterion.Order.desc(o.getProperty()) :
                    org.hibernate.criterion.Order.asc(o.getProperty()));
        }
        return criteria;
    }
    
    private Query createNamedQuery(String name, Map<String, Object> parameters) {
        Query query = getEntityManager().createNamedQuery(name);
        for( String param : parameters.keySet() ) {
            query.setParameter(param, parameters.get(param));
        }
        return query;
    }
    
    @SuppressWarnings("rawtypes")
    private List findByNamedQueryWithNoGeneric(String name, Map<String, Object> parameters) {
        return createNamedQuery(name, parameters).getResultList();
    }
    
    @SuppressWarnings("rawtypes")
    private List findByNamedQueryWithNoGeneric(String name, Map<String, Object> parameters, int pageSize, int pageNum) {
        Query query = createNamedQuery(name, parameters);
        
        int firstResult = pageNum < 1 ? 0 : pageNum * pageSize;
        query.setFirstResult(firstResult);
        query.setMaxResults(pageSize);
        
        return query.getResultList();
    }
    
    protected Class<T> getEntityClass() {
        return entityClass;
    }
    
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
