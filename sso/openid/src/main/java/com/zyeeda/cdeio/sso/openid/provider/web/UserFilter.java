/* $Id$ */

package com.zyeeda.cdeio.sso.openid.provider.web;


/**
 * User filter.
 *
 * @author $Author$
 *
 */
public class UserFilter extends XrdsFilter {

    @Override
    protected String getTemplatePath() {
        return "user.ftl";
    }

}
