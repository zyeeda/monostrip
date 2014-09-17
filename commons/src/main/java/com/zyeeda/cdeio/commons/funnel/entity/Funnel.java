package com.zyeeda.cdeio.commons.funnel.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.zyeeda.cdeio.commons.annotation.scaffold.Scaffold;
import com.zyeeda.cdeio.commons.base.entity.DomainEntity;
import com.zyeeda.cdeio.commons.organization.entity.Account;

@Entity
@Table(name = "FUL_FUNNEL")
@Scaffold("/funnel/funnel")
public class Funnel extends DomainEntity {

    private static final long serialVersionUID = -5800560027265954089L;
    
    private String name;

    private FunnelType type;
    
    private String dataId;
    
    private String description;
    
    private List<Account> accounts = new ArrayList<Account>();

    private List<FunnelData> datas = new ArrayList<FunnelData>();

    @ManyToOne
    @JoinColumn(name = "F_TYPE_ID")
    public FunnelType getType() {
        return type;
    }

    public void setType(FunnelType type) {
        this.type = type;
    }

    @Column(name = "F_DATA_ID", length = 40)
    public String getDataId() {
        return dataId;
    }


    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
    
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
    
    @ManyToMany
    @JoinTable(
            name = "FUL_FUNNEL_ACCOUNT",
            joinColumns = @JoinColumn(name = "F_FUNNEL_ACCOUNT_ID"),
            inverseJoinColumns = @JoinColumn(name = "F_FUNNEL_ID"))
    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
    
    @ManyToMany
    @JoinTable(
            name = "FUL_FUNNEL_DATA_REF",
            joinColumns = @JoinColumn(name = "F_FUNNEL_DATA_ID"),
            inverseJoinColumns = @JoinColumn(name = "F_FUNNEL_ID"))
    public List<FunnelData> getDatas() {
        return datas;
    }

    public void setDatas(List<FunnelData> datas) {
        this.datas = datas;
    }
}
