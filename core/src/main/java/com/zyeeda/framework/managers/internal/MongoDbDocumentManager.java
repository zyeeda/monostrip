package com.zyeeda.framework.managers.internal;

import static com.zyeeda.framework.managers.internal.MongoDbDocumentManagerHelper.document2GridFSFile;
import static com.zyeeda.framework.managers.internal.MongoDbDocumentManagerHelper.gridFSDBFile2Document;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.zyeeda.framework.entities.Document;
import com.zyeeda.framework.managers.DocumentException;
import com.zyeeda.framework.managers.DocumentManager;
import com.zyeeda.framework.nosql.MongoDbService;

public class MongoDbDocumentManager implements DocumentManager {

	private static final Logger logger = LoggerFactory.getLogger(MongoDbDocumentManager.class);
	
	//private final static String COLLECTION_NAME = "docs";
	
	private MongoDbService mongodbSvc;
	
	public MongoDbDocumentManager(MongoDbService mongodbSvc) {
		this.mongodbSvc = mongodbSvc;
	}
	
	/*
	@Deprecated
	private DBCollection getCollection() {
		DB db = this.mongodbSvc.getDefaultDatabase();
		DBCollection collection = db.getCollection(COLLECTION_NAME);
		return collection;
	}
	*/
	
	private DBCollection getFilesCollection() {
		DB db = this.mongodbSvc.getDefaultDatabase();
		DBCollection collection = db.getCollection("fs.files");
		return collection;
	}
	
	@Override
	public void persist(Document document) {
		GridFS fs = new GridFS(this.mongodbSvc.getDefaultDatabase());
		GridFSFile file = document2GridFSFile(fs, document);
		logger.info("saving uplaoded file {} ...", document.getName());
		file.save();
		logger.info("file {} saved", document.getName());
		
		document.setId(file.getId().toString());
	}
	
	@Override
	public Document findById(String id, boolean includeContent) {
		DBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(id));
		
		GridFS fs = new GridFS(this.mongodbSvc.getDefaultDatabase());
		GridFSDBFile file = fs.findOne(query);
		
		return gridFSDBFile2Document(file, includeContent);
	}
	
	@Override
	public Document findByIdAndFileName(String id, String fileName) {
		logger.trace("find by id [{}] and file name [{}]", id, fileName);
		DBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(id));
		query.put("filename", fileName);
		
		GridFS fs = new GridFS(this.mongodbSvc.getDefaultDatabase());
		GridFSDBFile file = fs.findOne(query);
		
		return gridFSDBFile2Document(file);
	}
	
	@Override
	public void removeById(String id) {
		logger.trace("remove by id = {}", id);
		GridFS fs = new GridFS(this.mongodbSvc.getDefaultDatabase());
		fs.remove(new ObjectId(id));
	}
	@Override
	public long countByForeignId(String foreignId) {
		DBObject query = new BasicDBObject();
		query.put("foreignId", foreignId);
		DBCollection c = this.getFilesCollection();
		return c.count(query);
	}
	
	@Override
	public void replaceForeignId(String oldForeignId, String newForeignId) {
		DBObject query = new BasicDBObject();
		query.put("foreignId", oldForeignId);
		DBObject update = new BasicDBObject(
				"$set", new BasicDBObjectBuilder()
				.add("foreignId", newForeignId)
				.add("isTemp", false).get());
		
		DBCollection collection = this.getFilesCollection();
		collection.updateMulti(query, update);
	}
	
	@Override
	public List<Document> findByForeignId(String foreignId) {
		DBObject query = new BasicDBObject();
		query.put("foreignId", foreignId);
		GridFS fs = new GridFS(this.mongodbSvc.getDefaultDatabase());
		List<GridFSDBFile> files = fs.find(query);
		
		List<Document> docs = new ArrayList<Document>(files.size());
		for (GridFSDBFile file : files) {
			docs.add(gridFSDBFile2Document(file, false));
		}
		
		return docs;
	}
	
	@Override
	public void eraseTemp() {
		DBObject query = new BasicDBObject();
		query.put("isTemp", true);
		GridFS fs = new GridFS(this.mongodbSvc.getDefaultDatabase());
		fs.remove(query);
	}
	@Override
	public long countByIsTemp() {
		DBObject query = new BasicDBObject();
		query.put("isTemp", true);
		DBCollection c = this.getFilesCollection();
		return c.count(query);
	}
	@Override
	public long countBySuffixes(String foreignId, String tempForeignId, String... suffixes) {
		DBObject query = new BasicDBObjectBuilder().push("foreignId").add("$in", new String[] { foreignId, tempForeignId }).pop()
			.push("fileType").add("$in", suffixes).get();
		logger.debug("bson query = {}", query);
		DBCollection c = this.getFilesCollection();
		return c.count(query);
	}

	@Override
	public void copyFile(String oldForeignId, String newForeignId) {
		List<Document> docs = this.findByForeignId(oldForeignId);
		DBObject query = new BasicDBObject();
		
		GridFS fs = new GridFS(this.mongodbSvc.getDefaultDatabase());
		for (Document doc : docs) {
			query.put("_id", new ObjectId(doc.getId()));
			GridFSDBFile file = fs.findOne(query);
			doc.setForeignId(newForeignId);
			logger.info("save file id is  {}" , newForeignId);
			InputStream in = file.getInputStream();
			doc.setContent(in);
			this.persist(doc);
		}
	}
	
	@Override
	public void updateDocument(Document document) throws UnknownHostException,
			DocumentException {
		GridFS fs = new GridFS(this.mongodbSvc.getDefaultDatabase());
		GridFSDBFile file =  fs.find(new ObjectId(document.getId()));
		file.put("description", document.getDescription());
		file.save();
	}
	
	
	/*
	private List<Document> find(Map<String, Object> map,
			Map<String, Object> map1, String foreignId, int skip, int limit)
			throws DocumentException, MongoException {
		try {
			DBCollection collection = this.getCollection();
			GridFS gridFS = new GridFS(collection.getDB());
			BasicDBObject basicDBObject = new BasicDBObject();
			if (StringUtils.isNotBlank(foreignId)) {
				basicDBObject.put("foreignId", foreignId);
			}
			if (map != null) {
				basicDBObject.put((String) map.get("condition"), map
						.get("value"));
			}
			if (map1 != null) {
				DBObject in = new BasicDBObject("$in", map1.get("value"));
				basicDBObject.put((String) map1.get("condition"), in);
			}
			BasicDBObject condition = new BasicDBObject();
			condition.put("lastModifiedTime", -1);// 排序的元素 根据时间从大到小
			DBCursor cursor = gridFS.getFileList(basicDBObject).sort(condition)
					.skip(skip).limit(limit);
			List<Document> documentList = new ArrayList<Document>(cursor
					.count());
			while (cursor.hasNext()) {
				Document document = new Document();
				GridFSDBFile gridFSDBFile = (GridFSDBFile) cursor.next();
				document.setId(gridFSDBFile.getId().toString());
				document.setName(gridFSDBFile.getFilename());
				document.setDescription((String) gridFSDBFile
						.get("description"));
				document.setCreator((String) gridFSDBFile.get("creator"));
				document.setFileType((String) gridFSDBFile.get("fileType"));
				document.setFileSize(gridFSDBFile.getLength());
				document.setCreatedTime(gridFSDBFile.getUploadDate());
				document.setLastModifier((String) gridFSDBFile
						.get("lastModifier"));
				document.setLastModifiedTime((Date) gridFSDBFile
						.get("lastModifiedTime"));
				document.setForeignId((String) gridFSDBFile.get("foreignId"));
				Object obj = gridFSDBFile.get("weight");
				if (obj != null) {
					document.setWeight(Integer.parseInt(obj.toString()));
				}
				document.setCreatedTime(gridFSDBFile.getUploadDate());
				document.setOwner((String) gridFSDBFile.get("owner"));
				document.setFileType((String) gridFSDBFile.get("fileType"));
				document.setKeyword((String) gridFSDBFile.get("keyword"));
				document.setContentType(gridFSDBFile.getContentType());
				document.setSubType((String) gridFSDBFile.get("subType"));
				document.setPrimaryType((String) gridFSDBFile.get("primaryType"));
				documentList.add(document);
			}
			return documentList;
		} catch (Exception e) {
			throw new DocumentException(e);
		}
	}

	@Override
	public List<Document> findByKeyword(String owner, String[] keyword,
			String foreignId, int skip, int limit) throws DocumentException {
		try {
			Map<String, Object> map = null;
			Map<String, Object> map1 = null;
			if (StringUtils.isNotBlank(owner)) {
				map = new HashMap<String, Object>();
				map.put("condition", "owner");
				map.put("value", owner);
			}
			map1 = new HashMap<String, Object>();
			map1.put("condition", "keyword");
			map1.put("value", keyword);

			return this.find(map, map1, foreignId, skip, limit);
		} catch (MongoException e) {
			throw new DocumentException(e);
		}
	}

	

	// 批量删除
	@Override
	public void allremoveById(String[] id) throws DocumentException {
		logger.debug("remove by id = {}", id);
		try {
			DBCollection collection = this.getCollection();
			GridFS gridFS = new GridFS(collection.getDB());
			for (int i = 0; i < id.length; i++) {
				logger.debug("removeId = {}", id[i]);
				gridFS.remove(new ObjectId(id[i]));
			}
		} catch (MongoException e) {
			throw new DocumentException(e);
		}
	}

	@Override
	public int findNumber(String owner, String foreignId, String[] keyword)
			throws DocumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int findNumber(String foreignId, String[] keyword)
			throws DocumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int findOwnerNumber(String owner, String foreignId)
			throws DocumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Document> findByKeyword(String owner, String foreignId,
			int skip, int limit) throws DocumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateDocument(Document document) throws UnknownHostException,
			DocumentException {
		// TODO Auto-generated method stub
		
	}
	*/


	/*@Override
	public void updateDocument(Document document) throws DocumentException {
		try {
			logger.debug("updateDocument keyword={}", document.getKeyword());
			logger.debug("updateDocument name={}", document.getName());
			logger.debug("updateDocument desc={}", document.getDescription());
			Mongo mongo = new Mongo(addr, port);
			DBCollection collection = mongo.getDB(DB).getCollection("fs.files");
			BasicDBObject updateObject = new BasicDBObject();
			if (StringUtils.isNotBlank(document.getDescription())) {
				updateObject.put("description", document.getDescription());
			}
			if (StringUtils.isNotBlank(document.getKeyword())) {
				updateObject.put("keyword", document.getKeyword());
			}
			if (StringUtils.isNotBlank(document.getName())) {
				updateObject.put("filename", document.getName());
			}
			if (StringUtils.isNotBlank(document.getLastModifier())) {
				updateObject.put("lastModifier", document.getLastModifier());
			}
			if(document.getLastModifiedTime()!=null){
				updateObject.put("lastModifiedTime", document.getLastModifiedTime());
			}
			
			BasicDBObject basic = new BasicDBObject();
			basic.put("$set", updateObject);
			collection.update(new BasicDBObject("_id", new ObjectId(document
					.getId())), basic, false, true);
		} catch (MongoException e) {
			throw new DocumentException(e);
		} catch (UnknownHostException e) {
			throw new DocumentException(e);
		}
	}

	

//	@Override
//	public List<Document> findByKeyword(String owner, int skip, int limit)
//			throws DocumentException {
//		Map<String, Object> map = null;
//		Map<String, Object> map1 = null;
//		if (StringUtils.isNotBlank(owner)) {
//			map = new HashMap<String, Object>();
//			map.put("condition", "owner");
//			map.put("value", owner);
//		}
//		return this.find(map, map1, null, skip, limit);
//	}

	@Override
	public List<Document> findByKeyword(String owner, String foreignId,
			int skip, int limit) throws DocumentException {
		Map<String, Object> map = null;
		Map<String, Object> map1 = null;
		if (StringUtils.isNotBlank(owner)) {
			map = new HashMap<String, Object>();
			map.put("condition", "owner");
			map.put("value", owner);
		}
		return this.find(map, map1, foreignId, skip, limit);
	}

	@Override
	public int findNumber(String owner, String foreignId, String[] keyword)
			throws DocumentException {
		try {
			DBCollection collection = this.getCollection(addr, port, DB);
			GridFS gridFS = new GridFS(collection.getDB());
			BasicDBObject basicDBObject = new BasicDBObject();
			if (StringUtils.isNotBlank(foreignId)) {
				basicDBObject.put("foreignId", foreignId);
			}
			if (StringUtils.isNotBlank(owner)) {
				basicDBObject.put("owner", owner);
			}
			DBObject in = new BasicDBObject("$in", keyword);
			basicDBObject.put("keyword", in);
			return gridFS.find(basicDBObject).size();
		} catch (UnknownHostException e) {
			throw new DocumentException(e);
		}
	}

	@Override
	public int findNumber(String foreignId, String[] keyword)
			throws DocumentException {
		try {
			DBCollection collection = this.getCollection(addr, port, DB);
			GridFS gridFS = new GridFS(collection.getDB());
			BasicDBObject basicDBObject = new BasicDBObject();
			if (StringUtils.isNotBlank(foreignId)) {
				basicDBObject.put("foreignId", foreignId);
			}
			if (keyword.length > 1) {
				DBObject in = new BasicDBObject("$in", keyword);
				basicDBObject.put("keyword", in);
			}
			return gridFS.find(basicDBObject).size();
		} catch (UnknownHostException e) {
			throw new DocumentException(e);
		}
	}

	@Override
	public int findOwnerNumber(String owner, String foreignId)
			throws DocumentException {
		try {
			DBCollection collection = this.getCollection(addr, port, DB);
			GridFS gridFS = new GridFS(collection.getDB());
			BasicDBObject basicDBObject = new BasicDBObject();
			if (StringUtils.isNotBlank(foreignId)) {
				basicDBObject.put("foreignId", foreignId);
			}
			if (StringUtils.isNotBlank(owner)) {
				basicDBObject.put("owner", owner);
			}
			return gridFS.find(basicDBObject).size();
		} catch (UnknownHostException e) {
			throw new DocumentException(e);
		}
	}

	@Override
	public Document findById(String id) throws DocumentException {
		try {
			DBCollection collection = this.getCollection(addr, port, DB);
			GridFS gridFS = new GridFS(collection.getDB());
			GridFSDBFile gridFSDbFile = gridFS.find(new ObjectId(id));
			Document document = new Document();
			document.setId(gridFSDbFile.getId().toString());
			document.setName(gridFSDbFile.getFilename());
			document.setDescription((String) gridFSDbFile.get("description"));
			document.setCreator((String) gridFSDbFile.get("creator"));
			document.setCreatedTime((Date) gridFSDbFile.get("createdTime"));
			document.setLastModifier((String) gridFSDbFile.get("lastModifier"));
			document.setLastModifiedTime((Date) gridFSDbFile
					.get("lastModifiedTime"));
			document.setForeignId((String) gridFSDbFile.get("foreignId"));
			Object weightObj = gridFSDbFile.get("weight");
			if (weightObj != null) {
				document.setWeight(Integer.parseInt((weightObj.toString())));
			}
			document.setOwner((String) gridFSDbFile.get("owner"));
			// document.setFileSize(gridFSDbFile.getLength()); // 系统默认的
			document.setFileType((String) gridFSDbFile.get("fileType"));
			document.setKeyword((String) gridFSDbFile.get("keyword"));
			document.setContent(gridFSDbFile.getInputStream());
			document.setContentType(gridFSDbFile.getContentType());
			document.setSubType((String) gridFSDbFile.get("subType"));
			document.setType((String) gridFSDbFile.get("type"));
			return document;
		} catch (UnknownHostException e) {
			throw new DocumentException(e);
		} catch (MongoException e) {
			throw new DocumentException(e);
		}

	}*/

}
