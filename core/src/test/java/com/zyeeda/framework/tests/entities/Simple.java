package com.zyeeda.framework.tests.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.zyeeda.framework.commons.base.entity.SimpleDomainEntity;

@Entity
@Table(name="test_simple")
public class Simple extends SimpleDomainEntity {

    private static final long serialVersionUID = 8983808817663356876L;

}
