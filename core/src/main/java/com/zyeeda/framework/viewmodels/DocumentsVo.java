package com.zyeeda.framework.viewmodels;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "documentListView")
public class DocumentsVo {
	
	private int totalRecords;
	private int startIndex;
	private String sort;
	private String dir;
	private List<DocumentVo> records;
	
	public int getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
	
	public int getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	
	public List<DocumentVo> getRecords() {
		return records;
	}
	public void setRecords(List<DocumentVo> records) {
		this.records = records;
	}
	
}
