/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.web;

import com.zyeeda.cdeio.sso.openid.provider.OpenIdProvider;

/**
 * 携带 OpenIdProvider 的 BaseFilter.
 *
 * @author $Author$
 *
 */
public class OpenIdProviderAwareBaseFilter extends BaseFilter {

    /**
     * OpenID provider.
     */
    private OpenIdProvider openIdProvider;

    public void setOpenIdProvider(final OpenIdProvider openIdProvider) {
        this.openIdProvider = openIdProvider;
    }

    protected OpenIdProvider getOpenIdProvider() {
        return this.openIdProvider;
    }

}
