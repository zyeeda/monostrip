package com.zyeeda.framework.commons.base.data;

import java.util.List;

public interface TreeNode<E> {
    
    E getParent();
    void setParent(E parent);
    
    List<E> getChildren();
    void setChildren(List<E> children);
    
}
