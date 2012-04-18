package com.zyeeda.framework.managers.internal;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.zyeeda.framework.entities.Document;

public class MongoDbDocumentManagerHelper {
	
	private final static Logger logger = LoggerFactory.getLogger(MongoDbDocumentManagerHelper.class);

	public static GridFSFile document2GridFSFile(GridFS fs, Document document) {
		GridFSFile file = fs.createFile(document.getContent());
		
		file.put("isTemp", document.isTemp());
		file.put("filename", document.getName()); // 数据库自带filename
		file.put("description", document.getDescription());
		file.put("creator", document.getCreator());
		file.put("ownerChinaName", document.getOwnerChinaName());
		file.put("uploadDate", document.getCreatedTime()); // 数据库自带uploadDate
		file.put("lastModifier", document.getLastModifier());
		file.put("lastModifiedTime", document.getLastModifiedTime());
		file.put("fileforeignName", document.getFileName());
		file.put("foreignId", document.getForeignId());
		file.put("weight", document.getWeight());
		file.put("owner", document.getOwner());
		file.put("keyword", document.getKeyword());
		//file.put("description", document.getDescription());
		file.put("fileType", document.getFileType()); // 数据库自带fileType
		file.put("contentType", document.getContentType()); // 数据库自带contentType
		file.put("subType", document.getSubType());
		file.put("primaryType", document.getPrimaryType());
		
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("filename = " + document.getName() + "\n");
			sb.append("\tdescription = " + document.getDescription() + "\n");
			sb.append("\tcreator = " + document.getCreator() + "\n");
			sb.append("\tuploadDate = " + document.getCreatedTime() + "\n");
			sb.append("\tlastModifier = " + document.getLastModifier() + "\n");
			sb.append("\tlastModifiedTime = " + document.getLastModifiedTime() + "\n");
			sb.append("\tdescription = " + document.getDescription() + "\n");
			sb.append("\tforeignId = " + document.getForeignId() + "\n");
			sb.append("\tweight = " + document.getWeight() + "\n");
			sb.append("\towner = " + document.getOwner() + "\n");
			sb.append("\tkeyword = " + document.getKeyword() + "\n");
			sb.append("\tkeyword = " + document.getFileName() + "\n");
			sb.append("\tfileType = " + document.getFileType() + "\n");
			sb.append("\tcontentType = " +document.getContentType() + "\n");
			sb.append("\tprimaryType = " + document.getPrimaryType() + "\n");
			sb.append("\tsubType = " + document.getSubType() + "\n");
			
			logger.debug(sb.toString());
		}
		
		return file;
	}
	
	public static Document gridFSDBFile2Document(GridFSDBFile file, boolean includeContent) {
		Document document = new Document();
		
		document.setId(file.getId().toString());
		
		document.setName(file.getFilename());
		document.setDescription((String) file.get("description"));
		document.setCreator((String) file.get("creator"));
		document.setCreatedTime((Date) file.getUploadDate());
		document.setLastModifier((String) file.get("lastModifier"));
		document.setLastModifiedTime((Date) file.get("lastModifiedTime"));
		document.setOwnerChinaName((String)file.get("ownerChinaName"));
		document.setFileName((String)file.get("fileforeignName"));
		document.setForeignId((String) file.get("foreignId"));
		document.setWeight((Integer) file.get("weight"));
		document.setOwner((String) file.get("owner"));
		document.setKeyword((String) file.get("keyword"));
		document.setDescription((String) file.get("description"));
		document.setFileType((String) file.get("fileType"));
		document.setContentType(file.getContentType());
		document.setPrimaryType((String) file.get("primaryType"));
		document.setSubType((String) file.get("subType"));
		
		document.setFileSize(file.getLength());
		if (includeContent) {
			document.setContent(file.getInputStream());
		}
		
		return document;
	}
	
	public static Document gridFSDBFile2Document(GridFSDBFile file) {
		return gridFSDBFile2Document(file, true);
	}
	
}
