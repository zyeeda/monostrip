// Copyright 2011, Zyeeda, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Zyeeda, Inc.

package com.zyeeda.framework.account.internal;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.zyeeda.framework.account.AccountService;
import com.zyeeda.framework.service.AbstractService;

/**
 * account support service implement 
 *
 * @author Qi Zhao
 * @date 2011-06-15
 *
 * @LastChanged
 * @LastChangedBy $LastChangedBy:  $
 * @LastChangedDate $LastChangedDate:  $
 * @LastChangedRevision $LastChangedRevision:  $
 */
public class SystemAccountServiceProvider extends AbstractService implements AccountService, EnvironmentAware {

    private Environment environment = null;
    
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Map<String, String> getMockSignInConfig(String prefix) {
        Map<String, String> result = new HashMap<String, String>();
        result.put(prefix, environment.getProperty(prefix));
        return result;
    }
}

