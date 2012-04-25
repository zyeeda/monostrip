package com.zyeeda.framework.entities;

import java.io.InputStream;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.zyeeda.framework.entities.base.RevisionDomainEntity;

@Entity
@Table(name = "ZDA_SYS_DOCUMENTS")
@XmlRootElement(name = "document")
public class Document extends RevisionDomainEntity {

	private static final long serialVersionUID = -5913731949268189623L;
	
	// 外键
	private String foreignId;
	// 排序
	private int weight;
	// 所有者
	private String owner;
	// 文件大小
    private long fileSize;
    // 文件类型，保存文件扩展名
    private String fileType;
    // 文件类型（完整的  Media Type）
    private String contentType;
    // 文件类型（Media Type 的主类型）
    private String primaryType;
    // 文件类型（Media Type 的子类型）
    private String subType;
    // 关键字
    private String keyword;
    // 类别
    private String category;
    // 是否为临时文件
    private boolean temp;
    //备注
    private String description;

	//所有人中文名字
    private String ownerChinaName;
    //文件名称
    private String fileName;
   
	// 文件内容
    private InputStream content;
    
    public String getOwnerChinaName() {
		return ownerChinaName;
	}

	public void setOwnerChinaName(String ownerChinaName) {
		this.ownerChinaName = ownerChinaName;
	}

	public String getForeignId() {
		return foreignId;
	}

	public void setForeignId(String foreignId) {
		this.foreignId = foreignId;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getPrimaryType() {
		return primaryType;
	}

	public void setPrimaryType(String primaryType) {
		this.primaryType = primaryType;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public InputStream getContent() {
		return content;
	}

	public void setContent(InputStream content) {
		this.content = content;
	}
	
	public boolean isTemp() {
		return temp;
	}

	public void setTemp(boolean temp) {
		this.temp = temp;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
