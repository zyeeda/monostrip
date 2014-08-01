package com.zyeeda.coala.commons.funnel.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.zyeeda.coala.commons.annotation.scaffold.Scaffold;
import com.zyeeda.coala.commons.base.entity.DomainEntity;


@Entity
@Table(name = "FUL_FUNNEL_DATA_TYPE")
@Scaffold("/funnel/funnel-data-type")
public class FunnelDataType extends DomainEntity {

    private static final long serialVersionUID = -6969978376178996818L;

    private String name;
    
    private String feature;

    @NotNull
    @Column(name = "F_NAME", length = 100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "F_FEATURE", length = 100)
    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }
    
}
