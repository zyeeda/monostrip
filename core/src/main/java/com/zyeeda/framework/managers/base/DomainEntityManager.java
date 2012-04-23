package com.zyeeda.framework.managers.base;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.EmbeddedId;
import javax.persistence.Query;

import com.googlecode.genericdao.dao.jpa.GenericDAOImpl;
import com.googlecode.genericdao.search.jpa.JPAAnnotationMetadataUtil;
import com.googlecode.genericdao.search.jpa.JPASearchProcessor;
import com.zyeeda.framework.persistence.PersistenceService;
import com.zyeeda.framework.viewmodels.QueryResult;

@SuppressWarnings("unchecked")
public class DomainEntityManager<T, ID extends Serializable> extends GenericDAOImpl<T, ID> {

	private PersistenceService persistenceSvc;
//	protected Class<T> entityClass = (Class<T>) ((ParameterizedType) this
//			.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//	
	protected Class<T> entityClass;
	protected String entityName;
	protected String queryString = "";
	protected String countString = "";
	
	public DomainEntityManager(PersistenceService persistenceSvc) {
		this.persistenceSvc = persistenceSvc;
		this.setEntityManager(this.persistenceSvc.getCurrentSession());
		//HibernateEntityManagerFactory emf = (HibernateEntityManagerFactory) this.persistenceSvc.getSessionFactory();
		//MetadataUtil util = HibernateMetadataUtil.getInstanceForEntityManagerFactory(emf);
		JPAAnnotationMetadataUtil util = new JPAAnnotationMetadataUtil();
		JPASearchProcessor processor = new JPASearchProcessor(util);
		this.setSearchProcessor(processor);
		
		this.entityClass = this.getClassName(getClass());
		this.entityName = this.entityClass.getName();
		this.queryString = "select o from " + this.entityName + " o ";
		this.countString = " select count(o) from " + this.entityName + " o ";
	}
	
	protected PersistenceService getPersistenceService() {
		return this.persistenceSvc;
	}

	public Class<T> getClassName(Class<?> clazz) {
		Type[] types = ((ParameterizedType) clazz.getGenericSuperclass())
				.getActualTypeArguments();
		return (Class<T>) types[0];
	}
	
	@Deprecated
	protected static void setQueryParams(Query query, Object[] queryParams) {
		if (queryParams != null && queryParams.length > 0) {
			for (int i = 0; i < queryParams.length; i++) {
				query.setParameter(i + 1, queryParams[i]);
			}
		}
	}
	
	/**
	 * 组装order by语句
	 * 
	 * @param orderby
	 * @return
	 */
	@Deprecated
	protected static String buildOrderBy(LinkedHashMap<String, Object> orderby) {
		StringBuffer orderbyql = new StringBuffer("");
		if (orderby != null && orderby.size() > 0) {
			orderbyql.append(" order by ");
			for (String key : orderby.keySet()) {
				orderbyql.append("o.").append(key).append(" ").append(
						orderby.get(key)).append(",");
			}
			orderbyql.deleteCharAt(orderbyql.length() - 1);
		}

		return orderbyql.toString();
	}

	@Deprecated
	protected static <E> String getCountField(Class<E> clazz) {
		String out = "o";
		try {
			PropertyDescriptor[] propertyDescriptors = Introspector
					.getBeanInfo(clazz).getPropertyDescriptors();
			for (PropertyDescriptor propertydesc : propertyDescriptors) {
				Method method = propertydesc.getReadMethod();
				if (method != null
						&& method.isAnnotationPresent(EmbeddedId.class)) {
					PropertyDescriptor[] ps = Introspector.getBeanInfo(
							propertydesc.getPropertyType())
							.getPropertyDescriptors();
					out = "o."
							+ propertydesc.getName()
							+ "."
							+ (!ps[1].getName().equals("class") ? ps[1]
									.getName() : ps[0].getName());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return out;
	}

	@SuppressWarnings({ "deprecation" })
	public List<T> getResultList(Integer startIndex,
			Integer maxResult, String whereHql, Object[] params,
			LinkedHashMap<String, Object> orderBy) {
		String hql = queryString + (whereHql == null || "".equals(whereHql.trim()) ? "" : " where "
		        + whereHql) + buildOrderBy(orderBy);
		Query query = em().createQuery(hql);
		setQueryParams(query, params);
		if (startIndex != -1 && maxResult != -1) {
			query.setFirstResult(startIndex).setMaxResults(maxResult);
		}
		return query.getResultList();
	}
	
	@SuppressWarnings("deprecation")
	public int getCount(String whereHql, Object[] params) {
		StringBuffer hql = new StringBuffer();
		hql.append(this.countString).append(whereHql == null || "".equals(whereHql) ? "" : " where " + whereHql);
		Query query = em().createQuery(hql.toString());
		setQueryParams(query, params);
		return ((Long) query.getSingleResult()).intValue();
	}
	
	public QueryResult<T> getScrollData(Integer startIndex, Integer maxResult,
			String whereHql, Object[] params,
			LinkedHashMap<String, Object> orderBy) {
		QueryResult<T> qr = new QueryResult<T>();
		qr.setResultList(this.getResultList(startIndex, maxResult, whereHql, params, orderBy));
		qr.setTotalRecords(this.getCount(whereHql, params));
		return qr;
	}
	
    public List<T> queryByHql(String hql, String[] params) {
        Query query = this.em().createQuery(hql);
        if (params != null) {
            for (int i = 0, il = params.length; i < il; i++) {
                query.setParameter(i + 1, params[i]);
            }
        }
        List<T> list = query.getResultList();
        return list;
    }
    
    public List<Object[]> queryBySql(String sql, String[] params) {
        Query query = this.em().createNativeQuery(sql);
        if (params != null) {
            for (int i = 0, il = params.length; i < il; i++) {
                query.setParameter(i + 1, params[i]);
            }
        }
        List<Object[]> list = query.getResultList();
        return list;
    }
}
