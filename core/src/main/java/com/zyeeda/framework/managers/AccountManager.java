// Copyright 2011, Zyeeda, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Zyeeda, Inc.

package com.zyeeda.framework.managers;

import java.util.List;

import com.zyeeda.framework.entities.Account;

/**
 * account manager  
 *
 * @author Qi Zhao
 * @date 2011-06-15
 *
 * @LastChanged
 * @LastChangedBy $LastChangedBy:  $
 * @LastChangedDate $LastChangedDate:  $
 * @LastChangedRevision $LastChangedRevision:  $
 */
public interface AccountManager {

    public List<Account> findByUserId(String userId) throws UserPersistException;
    
    public Account findByUserIdAndSystemName(String userId, String systemName) throws UserPersistException;

    public void update(Account account) throws UserPersistException;
    
    public void remove(String systemName) throws UserPersistException;
}
