package com.zyeeda.framework.commons.manager;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基础DAO接口
 * @author guyong
 * 
 */
/**
 * @author guyong
 *
 * @param <T>
 * @param <ID>
 */
public interface BaseManager<T, ID extends Serializable> {

    /**
     * 根据指定id获取实体对象
     * @param id 指定的id
     * @return 指定id对应的实体
     */
    T get(ID id);
    
    /**
     * 批量根据指导定id获取实体
     * @param ids 指定的id
     * @return 对应的实体
     */
    List<T> get(ID... ids);
    
    /**
     * 批量根据指导定id获取实体
     * @param ids 指定的id
     * @return 对应的实体
     */
    List<T> get(Collection<ID> ids);
    
    /**
     * 根据指定的id获取延迟加载的实体
     * @param id 指定的id
     * @return 能延迟加载的实体
     */
    T getReference(ID id);
    
    /**
     * 批量根据指定的id获取延迟加载的实体
     * @param id 指定的id
     * @return 能延迟加载的实体
     */
    List<T> getReference(ID... ids);

    /**
     * 批量根据指定的id获取延迟加载的实体
     * @param id 指定的id
     * @return 能延迟加载的实体
     */
    List<T> getReference(Collection<ID> ids);

    /**
     * 持久化实体
     * @param entity 需要持久化的实体
     * @return 持久化后的实体
     */
    T save(T entity);
    
    /**
     * 批量持久化实体
     * @param entity 需要持久化的实体
     * @return 持久化后的实体
     */
    List<T> save(T... entities);
    
    /**
     * 批量持久化实体
     * @param entity 需要持久化的实体
     * @return 持久化后的实体
     */
    List<T> save(Collection<T> entities);

    /**
     * 更新实体
     * @param entity 需要更新的实体
     * @return 更新后的实体
     */
    T update(T entity);
    
    /**
     * 批量更新实体
     * @param entity 需要更新的实体
     * @return 更新后的实体
     */
    List<T> update(T... entities);

    /**
     * 批量更新实体
     * @param entity 需要更新的实体
     * @return 更新后的实体
     */
    List<T> update(Collection<T> entities);

    /**
     * 删除实体
     * @param entity 需要删除的实体
     * @return 删除的实体
     */
    T remove(T entity);
    
    /**
     * 批量删除实体
     * @param entity 需要删除的实体
     * @return 删除的实体
     */
    List<T> remove(T... entities);
    
    /**
     * 批量删除实体
     * @param entity 需要删除的实体
     * @return 删除的实体
     */
    List<T> remove(Collection<T> entities);

    /**
     * 根据id删除实体
     * @param id 指定的id
     * @return 删除的实体
     */
    T removeById(ID id);
    
    /**
     * 批量根据id删除实体
     * @param id 指定的id
     * @return 删除的实体
     */
    List<T> removeById(ID... ids);

    /**
     * 批量根据id删除实体
     * @param id 指定的id
     * @return 删除的实体
     */
    List<T> removeById(Collection<ID> ids);
    
    /**
     * 获取所有实体
     * @param orders 指定的排序方式
     * @return 所有实体
     */
    List<T> list(Order... orders);

    /**
     * 获取所有实体, 可以分页
     * @param pageSize 每一页的记录数
     * @param pageNum 当前页
     * @param orders 指定的排序方式
     * @return 指定页的实体
     */
    List<T> list(int pageSize, int pageNum, Order... orders);

    /**
     * 根据给定样例实体的属性值查找
     * @param entity 样例实体
     * @param orders 排序方式
     * @return 查找到的实体
     */
    List<T> findByExample(T entity, Order... orders);
    
    /**
     * 根据给定样例实体的属性值查找
     * @param entity 样例实体
     * @param pageSize 每一页的记录数
     * @param pageNum 当前页
     * @param orders 排序方式
     * @return 查找到的实体
     */
    List<T> findByExample(T entity, int pageSize, int pageNum, Order... orders);
    
    /**
     * 执行配置的查询(named query)
     * @param name 配置的查询的名称
     * @param parameters 查询语句中的定义的变量的值
     * @return 查询语句的结果
     */
    List<T> findByNamedQuery(String name, Map<String, Object> parameters);
    
    /**
     * 执行配置的查询(named query)
     * @param name 配置的查询的名称
     * @param parameters 查询语句中的定义的变量的值
     * @param pageSize 每一页的记录数
     * @param pageNum 当前页
     * @return 查询语句的结果
     */
    List<T> findByNamedQuery(String name, Map<String, Object> parameters, int pageSize, int pageNum);
    
    /**
     * 执行配置的查询(named query), 返回值可以不是当前dao对应的实体
     * @param name 配置的查询的名称
     * @param parameters 查询语句中的定义的变量的值
     * @return 查询语句的结果
     */
    List<?> findUntypedByNamedQuery(String name, Map<String, Object> parameters);
    
    /**
     * 执行配置的查询(named query), 返回值可以不是当前dao对应的实体
     * @param name 配置的查询的名称
     * @param parameters 查询语句中的定义的变量的值
     * @param pageSize 每一页的记录数
     * @param pageNum 当前页
     * @return 查询语句的结果
     */
    List<?> findUntypedByNamedQuery(String name, Map<String, Object> parameters, int pageSize, int pageNum);
    
    /**
     * 根据给定样例实体的属性值查找, 并只返回一个实体
     * @param entity 样例实体
     * @param orders 排序方式
     * @return 查找到的实体
     */
    T findSingleResultByExample(T entity, Order... orders);
    
    /**
     * 根据给定样例实体的属性值查找, 并只返回一个实体
     * @param entity 样例实体
     * @param pageSize 每一页的记录数
     * @param pageNum 当前页
     * @param orders 排序方式
     * @return 查找到的实体
     */
    T findSingleResultByExample(T entity, int pageSize, int pageNum, Order... orders);
    
    /**
     * 执行配置的查询(named query), 并只返回一个实体
     * @param name 配置的查询的名称
     * @param parameters 查询语句中的定义的变量的值
     * @return 查询语句的结果
     */
    Object findSingleResultByNamedQuery(String name, Map<String, Object> parameters);
    
    /**
     * 执行配置的查询(named query), 并只返回一个实体
     * @param name 配置的查询的名称
     * @param parameters 查询语句中的定义的变量的值
     * @param pageSize 每一页的记录数
     * @param pageNum 当前页
     * @return 查询语句的结果
     */
    Object findSingleResultByNamedQuery(String name, Map<String, Object> parameters, int pageSize, int pageNum);
    
    /**
     * 判断指定实体对象的指定属性是否唯一, 所指定的属性是or关系
     * @param entity 指导定的实体
     * @param properties 指定的属性
     * @return 如果指导定的属性都是唯一则返回true, 否则返回false
     *
     * boolean isUnique(T entity, String... properties);
     */
    
    /**
     * 执行named query, @see javax.persistence.Query.executeUpdate
     * @param name 配置的名称
     * @param parameters 对应的参数
     * @return
     */
    int executeNamedQuery(String name, Map<String, Object> parameters);
    
    /**
     * 同步当前上下文与持久层
     */
    void flush();
    
    /**
     * 重新从持久层取指定的实体
     * @param entity 指定的实体
     * @return
     */
    T refresh(T entity);
    
    /**
     * 批量重新从持久层取指定的实体
     * @param entities
     * @return
     */
    List<T> refresh(T... entities);
    
    /**
     * 排序的描叙对象
     * @author guyong
     *
     */
    class Order {
        
        private String property = null;
        private boolean desc = true;

        /**
         * 实例化
         * @param property 排序的属性
         * @param desc 排序方式
         */
        public Order(String property, boolean desc) {
            this.property = property;
            this.desc = desc;
        }
        
        public Order(String property) {
            this.property = property;
        }
        
        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public boolean isDesc() {
            return desc;
        }

        public void setDesc(boolean desc) {
            this.desc = desc;
        }
        
        public static Order create(String property, boolean desc) {
            return new Order(property, desc);
        }
    }
}
