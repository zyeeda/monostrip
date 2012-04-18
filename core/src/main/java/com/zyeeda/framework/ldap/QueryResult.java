package com.zyeeda.framework.ldap;

import java.util.ArrayList;
import java.util.List;

public class QueryResult<T> {
	private Long totalRecords;
	private List<T> resultList = new ArrayList<T>();

	public Long getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(Long totalRecords) {
		this.totalRecords = totalRecords;
	}

	public List<T> getResultList() {
		return resultList;
	}

	public void setResultList(List<T> resultList) {
		this.resultList = resultList;
	}

}
