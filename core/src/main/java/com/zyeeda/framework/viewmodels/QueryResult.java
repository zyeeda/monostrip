package com.zyeeda.framework.viewmodels;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "queryResult")
public class QueryResult<T> {

	private int totalRecords;
	private List<T> resultList = new ArrayList<T>();
	private Integer pageViewCount;

	
	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public List<T> getResultList() {
		return resultList;
	}

	public void setResultList(List<T> resultList) {
		this.resultList = resultList;
	}

	public Integer getPageViewCount() {
		return pageViewCount;
	}

	public void setPageViewCount(Integer pageViewCount) {
		this.pageViewCount = pageViewCount;
	}

}
