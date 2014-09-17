package com.zyeeda.cdeio.tests.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.zyeeda.cdeio.commons.base.entity.DomainEntity;

@Entity
@Table(name="test_simple")
public class Simple extends DomainEntity {

    private static final long serialVersionUID = 8983808817663356876L;

}
