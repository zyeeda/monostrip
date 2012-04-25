// Copyright 2011, Zyeeda, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Zyeeda, Inc.

package com.zyeeda.framework.account;

import java.util.Map;

import com.zyeeda.framework.service.Service;

/**
 * account support service 
 *
 * @author Qi Zhao
 * @date 2011-06-15
 *
 * @LastChanged
 * @LastChangedBy $LastChangedBy:  $
 * @LastChangedDate $LastChangedDate:  $
 * @LastChangedRevision $LastChangedRevision:  $
 */
public interface AccountService extends Service {

    public Map<String, String> getMockSignInConfig(String prefix);
}
