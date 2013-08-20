package com.zyeeda.coala.commons.authc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.zyeeda.coala.commons.annotation.scaffold.Scaffold;
import com.zyeeda.coala.commons.base.entity.DomainEntity;

@Entity
@Table(name = "ZDA_PERMISSION")
@Scaffold("/system/permissions")
public class Permission extends DomainEntity {

    private static final long serialVersionUID = -1467393705578439897L;

    private String name = null;
    private String value = null;
    private String description = null;
    private Boolean scaffold = null;

    @Column(name = "F_NAME", length = 200)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "F_DESC", length = 2000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "F_VALUE", length = 200)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Column(name = "F_SCAFFOLD")
    public Boolean getScaffold() {
        return scaffold;
    }

    public void setScaffold(Boolean scaffold) {
        this.scaffold = scaffold;
    }

}
