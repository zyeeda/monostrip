/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider;

import java.util.Date;

/**
 * 活动站点.
 *
 * @author $Author$
 *
 */
public class ActiveSite {

    /**
     * 站点名称.
     */
    private String name;

    /**
     * 登录时间.
     */
    private Date signInTime;

    /**
     * 站点首页地址.
     */
    private String indexUrl;

    /**
     * 站点登出地址.
     */
    private String signOutUrl;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getSignInTime() {
        return this.signInTime;
    }

    public void setSignInTime(final Date signInTime) {
        this.signInTime = signInTime;
    }

    public String getIndexUrl() {
        return this.indexUrl;
    }

    public void setIndexUrl(final String indexUrl) {
        this.indexUrl = indexUrl;
    }

    public String getSignOutUrl() {
        return this.signOutUrl;
    }

    public void setSignOutUrl(final String signOutUrl) {
        this.signOutUrl = signOutUrl;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.indexUrl == null) ? 0 : this.indexUrl.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result
                + ((this.signInTime == null) ? 0 : this.signInTime.hashCode());
        result = prime * result
                + ((this.signOutUrl == null) ? 0 : this.signOutUrl.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ActiveSite other = (ActiveSite) obj;
        if (this.indexUrl == null) {
            if (other.indexUrl != null) {
                return false;
            }
        } else if (!this.indexUrl.equals(other.indexUrl)) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.signInTime == null) {
            if (other.signInTime != null) {
                return false;
            }
        } else if (!this.signInTime.equals(other.signInTime)) {
            return false;
        }
        if (this.signOutUrl == null) {
            if (other.signOutUrl != null) {
                return false;
            }
        } else if (!this.signOutUrl.equals(other.signOutUrl)) {
            return false;
        }
        return true;
    }

}
