package com.zyeeda.coala.commons.resource.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.zyeeda.coala.commons.annotation.scaffold.Scaffold;
import com.zyeeda.coala.commons.base.entity.DomainEntity;

/**
 * 菜单项.
 *
 * @author $Author$
 *
 */
@Entity
@Table(name = "ZDA_MENUITEM")
@Scaffold("/system/menu")
public class MenuItem extends DomainEntity {

    /**
     * 自动生成的序列化版本 UID.
     */
    private static final long serialVersionUID = -9455635438606377L;

    /**
     * 菜单项名称.
     */
    private String name;

    /**
     * 菜单项描述.
     */
    private String description;

    /**
     * Feature 路径.
     */
    private String featurePath;

    /**
     * Icon class.
     */
    private String iconClass;

    /**
     * 选项.
     */
    private String option;

    /**
     * 父节点菜单项.
     */
    private MenuItem parent;

    @Column(name = "F_NAME", length = 100)
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

    @Column(name = "F_FEATURE_PATH", length = 200)
    public String getFeaturePath() {
        return this.featurePath;
    }

    public void setFeaturePath(final String featurePath) {
        this.featurePath = featurePath;
    }

    @Column(name = "F_ICON", length = 100)
    public String getIconClass() {
        return this.iconClass;
    }

    public void setIconClass(final String iconClass) {
        this.iconClass = iconClass;
    }

    @Column(name = "F_OPTION", length = 2000)
    public String getOption() {
        return this.option;
    }

    public void setOption(final String option) {
        this.option = option;
    }

    @ManyToOne
    @JoinColumn(name = "F_PARENT_ID")
    public MenuItem getParent() {
        return this.parent;
    }

    public void setParent(final MenuItem parent) {
        this.parent = parent;
    }

}
