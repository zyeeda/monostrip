package com.zyeeda.coala.commons.funnel.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.zyeeda.coala.commons.annotation.scaffold.Scaffold;
import com.zyeeda.coala.commons.base.entity.DomainEntity;

@Entity
@Table(name = "FUL_FUNNEL_DATA")
@Scaffold("/funnel/funnel-data")
public class FunnelData extends DomainEntity {

    private static final long serialVersionUID = -2322334909170543063L;

    private String name;

    private String dataId;
    
    private String description;
    
    private FunnelDataType dataType;
    
    @Column(name = "F_NAME", length = 100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Column(name = "F_DESC", length = 3000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Column(name = "F_DATA_ID", length = 40)
    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    @ManyToOne
    @JoinColumn(name = "F_TYPE_ID")
    public FunnelDataType getDataType() {
        return dataType;
    }

    public void setDataType(FunnelDataType dataType) {
        this.dataType = dataType;
    }
    
}
