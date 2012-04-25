package com.zyeeda.framework.viewmodels;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.zyeeda.framework.entities.Account;

@XmlRootElement(name="avo")
public class AccountVo {
	
	private List<Account> accounts;

	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}
}
