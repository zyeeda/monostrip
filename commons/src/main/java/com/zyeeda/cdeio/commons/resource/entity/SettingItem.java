package com.zyeeda.cdeio.commons.resource.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.zyeeda.cdeio.commons.annotation.scaffold.Scaffold;
import com.zyeeda.cdeio.commons.base.entity.DomainEntity;

/**
 * 配置项.
 *
 * @author $Author$
 *
 */
@Entity
@Table(name = "ZDA_SETTINGITEM")
@Scaffold("/system/settings")
public class SettingItem extends DomainEntity {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = -6881118213199712939L;

    /**
     * 配置项名称.
     */
    private String name;

    /**
     * 配置项描述.
     */
    private String description;

    /**
     * 配置项内容.
     */
    private String value;

    @Column(name = "F_VALUE", length = 1000)
    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Column(name = "F_NAME", length = 200)
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Column(name = "F_DESC", length = 2000)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}
