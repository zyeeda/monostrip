package com.zyeeda.framework.entities.base;

import java.util.Set;

/**
 * @author guyong
 *
 */
public interface TreeStyleEntity<E> {

    E getParent();
    void setParent(E parent);
    
    Set<E> getChildren();
    void setChildren(Set<E> children);
    
}
